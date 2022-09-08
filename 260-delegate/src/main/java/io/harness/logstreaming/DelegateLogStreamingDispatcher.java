/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

// rename package
package io.harness.logstreaming;

import io.harness.network.SafeHttpCall;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
  private Map<String, Boolean> shouldCloseStream = new ConcurrentHashMap<>();
  ReadWriteLock readWriteLockForMap = new ReentrantReadWriteLock();
  ReadWriteLock readWriteLockForToken = new ReentrantReadWriteLock();

  private class DelegateLogStreamingDispatcherService extends AbstractScheduledService {
    @Override
    protected void runOneIteration() throws Exception {
      log.info("Current pool size of logStreamingExecutor is {}", logStreamingExecutor.getActiveCount());
      swapMapsAndDispatchLogs();
    }

    @Override
    protected Scheduler scheduler() {
      return Scheduler.newFixedRateSchedule(0, 2, TimeUnit.SECONDS);
    }
  }

  public void swapMapsAndDispatchLogs() {
    if (logCache.isEmpty()) {
      return;
    }
    Map<String, List<LogLine>> logCacheToBeFlushed;
    try {
      readWriteLockForMap.writeLock().lock();
      logCacheToBeFlushed = logCache;
      logCache = new HashMap<>();
    } finally {
      readWriteLockForMap.writeLock().unlock();
    }
    logCacheToBeFlushed.forEach(
        (logKey, logLines) -> logStreamingExecutor.submit(() -> sendLogsOverHttpAndCloseStream(logKey, logLines)));
  }

  public void saveLogsInCache(String logKey, LogLine logLine) {
    try {
      readWriteLockForMap.readLock().lock();
      if (!logCache.containsKey(logKey)) {
        logCache.put(logKey, new ArrayList<>());
      }
      logCache.get(logKey).add(logLine);
    } finally {
      readWriteLockForMap.readLock().unlock();
    }
  }

  // this is just a enforcement to dispatch logs quickly, otherwise logs will anyhow be dispatched in next iteration.
  public void forceDispatchLogsBeforeClosingStream(String logKey) {
    try {
      readWriteLockForMap.readLock().lock();
      if (!logCache.containsKey(logKey)) {
        logCache.put(logKey, new ArrayList<>());
      }
      List<LogLine> logLines = logCache.get(logKey);
      logCache.remove(logKey);
      shouldCloseStream.put(logKey, true);
      logStreamingExecutor.submit(() -> sendLogsOverHttpAndCloseStream(logKey, logLines));
    } finally {
      readWriteLockForMap.readLock().unlock();
    }
  }

  private void sendLogsOverHttpAndCloseStream(String logKey, List<LogLine> logLines) {
    try {
      readWriteLockForToken.readLock().lock();
      SafeHttpCall.executeWithExceptions(logStreamingClient.pushMessage(token, accountId, logKey, logLines));
    } catch (Exception ex) {
      log.error("Unable to push message to log stream for account {} and key {}", accountId, logKey, ex);
    } finally {
      if (shouldCloseStream.containsKey(logKey)) {
        closeStream(logKey);
      }
      readWriteLockForToken.readLock().unlock();
    }
  }

  private void closeStream(String logKey) {
    try {
      SafeHttpCall.executeWithExceptions(logStreamingClient.closeLogStream(token, accountId, logKey, true));
    } catch (Exception ex) {
      log.error("Unable to close log stream for account {} and key {}", accountId, logKey, ex);
    } finally {
      shouldCloseStream.remove(logKey);
    }
  }

  public void start(String accountId) {
    if (running.compareAndSet(false, true)) {
      this.accountId = accountId;
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

  public void setToken(String newToken) {
    if (!newToken.equals(this.token)) {
      try {
        readWriteLockForToken.writeLock().lock();
        this.token = newToken;
      } finally {
        readWriteLockForToken.writeLock().unlock();
      }
    }
  }
}
