/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

// rename package
package io.harness.logstreaming;

import io.harness.logging.LoggingListener;
import io.harness.network.SafeHttpCall;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DelegateLogStreamingDispatcher {
  private String accountId;
  private String token;
  @Inject private LogStreamingClient logStreamingClient;

  @Inject @Named("logStreamingExecutor") private ThreadPoolExecutor logStreamingExecutor;

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicReference<DelegateLogStreamingDispatcherService> svcHolder = new AtomicReference<>();

  private Map<String, List<LogLine>> logCache = new HashMap<>();
  private Map<String, List<LogLine>> logCacheToBeFlushed;
  ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private class DelegateLogStreamingDispatcherService extends AbstractScheduledService {
    DelegateLogStreamingDispatcherService() {
      addListener(new LoggingListener(this), MoreExecutors.directExecutor());
    }

    @Override
    protected void runOneIteration() throws Exception {
      swapMapsAndDispatchLogs();
    }

    @Override
    protected Scheduler scheduler() {
      return Scheduler.newFixedRateSchedule(0, 2, TimeUnit.SECONDS);
    }
  }

  public void swapMapsAndDispatchLogs() {
    if (logCache.isEmpty() || token == null) {
      return;
    }
    try {
      readWriteLock.writeLock().lock();
      logCacheToBeFlushed = logCache;
      logCache = new HashMap<>();
    } finally {
      readWriteLock.writeLock().unlock();
    }
    dispatchLogs(logCacheToBeFlushed);
  }

  private void dispatchLogs(Map<String, List<LogLine>> logCache) {
    for (Iterator<Map.Entry<String, List<LogLine>>> iterator = logCache.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<String, List<LogLine>> next = iterator.next();
      logStreamingExecutor.submit(() -> sendLogsOverHttp(next.getKey(), next.getValue()));
      iterator.remove();
    }
  }

  public void saveLogsInCache(String logKey, LogLine logLine) {
    try {
      readWriteLock.readLock().lock();
      if (!logCache.containsKey(logKey)) {
        logCache.put(logKey, new ArrayList<>());
      }
      logCache.get(logKey).add(logLine);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  private void sendLogsOverHttp(String logKey, List<LogLine> logLines) {
    try {
      SafeHttpCall.executeWithExceptions(logStreamingClient.pushMessage(token, accountId, logKey, logLines));
    } catch (Exception ex) {
      log.error("Unable to push message to log stream for account {} and key {}", accountId, logKey, ex);
    }
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      log.info("Starting log streaming dispatcher.");
      DelegateLogStreamingDispatcherService delegateLogStreamingDispatcherService =
          new DelegateLogStreamingDispatcherService();
      delegateLogStreamingDispatcherService.startAsync();
      this.svcHolder.set(delegateLogStreamingDispatcherService);
    }
  }

  public void stop() {
    if (running.compareAndSet(true, false)) {
      log.info("Stopping log streaming dispatcher");
      DelegateLogStreamingDispatcherService delegateLogStreamingDispatcherService = this.svcHolder.get();
      delegateLogStreamingDispatcherService.stopAsync().awaitTerminated();
    }
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }
}
