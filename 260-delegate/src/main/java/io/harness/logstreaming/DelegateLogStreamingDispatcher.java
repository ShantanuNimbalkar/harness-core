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
import com.google.inject.Singleton;
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
  private final String accountId;
  private String token;
  private final LogStreamingClient logStreamingClient;

  private final ThreadPoolExecutor logStreamingExecutor;

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicReference<DelegateLogStreamingDispatcherService> svcHolder = new AtomicReference<>();

  private final AtomicBoolean isWritingInPrimaryLogCache = new AtomicBoolean(true);

  private final Map<String, List<LogLine>> primaryLogCache = new HashMap<>();
  private final Map<String, List<LogLine>> secondaryLogCache = new HashMap<>();

  ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  public DelegateLogStreamingDispatcher(
      String accountId, LogStreamingClient logStreamingClient, ThreadPoolExecutor logStreamingExecutor) {
    this.accountId = accountId;
    this.logStreamingClient = logStreamingClient;
    this.logStreamingExecutor = logStreamingExecutor;
  }

  private class DelegateLogStreamingDispatcherService extends AbstractScheduledService {
    // Question? what is its use?
    DelegateLogStreamingDispatcherService() {
      addListener(new LoggingListener(this), MoreExecutors.directExecutor());
    }

    @Override
    protected void runOneIteration() throws Exception {
      swapMapsAndDispatchLogs();
    }

    // question? what should be ideal time
    @Override
    protected Scheduler scheduler() {
      return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
  }

  public void swapMapsAndDispatchLogs() {
    // swap primary cache
    try {
      readWriteLock.writeLock().lock();
      if (isWritingInPrimaryLogCache.get()) {
        isWritingInPrimaryLogCache.set(false);
      } else {
        isWritingInPrimaryLogCache.set(true);
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }

    // now dispatch from secondary cache
    dispatchLogs(isWritingInPrimaryLogCache.get() ? secondaryLogCache : primaryLogCache);
  }

  private void dispatchLogs(Map<String, List<LogLine>> logCache) {
    if (logCache.isEmpty()) {
      return;
    }
    for (Iterator<Map.Entry<String, List<LogLine>>> iterator = logCache.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<String, List<LogLine>> next = iterator.next();
      // Question? are we able to send logs before removing from iterator
      logStreamingExecutor.submit(() -> sendLogsOverHttp(next.getKey(), next.getValue()));
      iterator.remove();
    }
  }

  public boolean dispatchAllLogsBeforeClosingStream(String logKey) {
    // it was currently writing in primaryLogCache, hence dispatch from primaryLogCache
    // no need of lock here since it will not be writing logs after calling closeStream
    if (isWritingInPrimaryLogCache.get()) {
      return dispatchAllLogsFromPrimaryCacheBeforeClosingStream(logKey, primaryLogCache);
    }
    return dispatchAllLogsFromPrimaryCacheBeforeClosingStream(logKey, secondaryLogCache);
  }

  private boolean dispatchAllLogsFromPrimaryCacheBeforeClosingStream(
      String logKey, Map<String, List<LogLine>> primaryLogCache) {
    if (!primaryLogCache.containsKey(logKey)) {
      return false;
    }
    logStreamingExecutor.submit(() -> sendLogsOverHttp(logKey, primaryLogCache.get(logKey)));
    primaryLogCache.remove(logKey);
    return true;
  }

  public void saveLogsInCache(String logKey, LogLine logLine) {
    try {
      readWriteLock.readLock().lock();
      if (isWritingInPrimaryLogCache.get()) {
        if (!primaryLogCache.containsKey(logKey)) {
          primaryLogCache.put(logKey, new ArrayList<>());
        }
        primaryLogCache.get(logKey).add(logLine);
      } else {
        if (!secondaryLogCache.containsKey(logKey)) {
          secondaryLogCache.put(logKey, new ArrayList<>());
        }
        secondaryLogCache.get(logKey).add(logLine);
      }
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  // Question? should we add a retry logic in case of failure
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
}
