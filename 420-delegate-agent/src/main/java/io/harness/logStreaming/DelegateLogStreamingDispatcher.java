package io.harness.logStreaming;

import io.harness.logging.LoggingListener;
import io.harness.logstreaming.LogLine;
import io.harness.logstreaming.LogStreamingClient;
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

  // save logs in primaryLogCache
  // give a better name
  private final AtomicBoolean isWritingInPrimaryLogCache = new AtomicBoolean(true);

  private final Map<String, List<LogLine>> primaryLogCache = new HashMap<>();
  private final Map<String, List<LogLine>> secondaryLogCache = new HashMap<>();

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
      swapMaps();
      if (isWritingInPrimaryLogCache.get()) {
        dispatchLogs(secondaryLogCache);
      } else {
        dispatchLogs(primaryLogCache);
      }
    }

    @Override
    protected Scheduler scheduler() {
      return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
  }

  private void dispatchLogs(Map<String, List<LogLine>> logCache) {
    if (logCache.isEmpty()) {
      return;
    }
    for (Iterator<Map.Entry<String, List<LogLine>>> iterator = logCache.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<String, List<LogLine>> next = iterator.next();
      // Question? are we able to send logs before removing from iterator
      logStreamingExecutor.submit(() -> sendLogsToLogStreaming(next.getKey(), next.getValue()));
      iterator.remove();
    }
  }

  // assumption is that no one is writing logs after calling closeStream
  public void dispatchAllLogsBeforeClosingStream(String logKey) {
    // it was currently writing in primaryLogCache, hence dispatch from primaryLogCache
    if (isWritingInPrimaryLogCache.get()) {
      dispatchAllLogsFromPrimaryCacheBeforeClosingStream(logKey, primaryLogCache);
    } else {
      dispatchAllLogsFromPrimaryCacheBeforeClosingStream(logKey, secondaryLogCache);
    }
  }

  private void dispatchAllLogsFromPrimaryCacheBeforeClosingStream(
      String logKey, Map<String, List<LogLine>> primaryLogCache) {
    if (!primaryLogCache.containsKey(logKey)) {
      return;
    }
    logStreamingExecutor.submit(() -> sendLogsToLogStreaming(logKey, primaryLogCache.get(logKey)));
    primaryLogCache.remove(logKey);
  }

  public void saveLogsInCache(String logKey, LogLine logLine) {
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
  }

  // Question? should we add a retry logic in case of failure
  private void sendLogsToLogStreaming(String logKey, List<LogLine> logLines) {
    try {
      SafeHttpCall.executeWithExceptions(logStreamingClient.pushMessage(token, accountId, logKey, logLines));
    } catch (Exception ex) {
      log.error("Unable to push message to log stream for account {} and key {}", accountId, logKey, ex);
    }
  }

  private void swapMaps() {
    if (isWritingInPrimaryLogCache.get()) {
      synchronized (primaryLogCache) {
        isWritingInPrimaryLogCache.set(false);
      }
    } else {
      synchronized (secondaryLogCache) {
        isWritingInPrimaryLogCache.set(true);
      }
    }
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      DelegateLogStreamingDispatcherService delegateLogStreamingDispatcherService =
          new DelegateLogStreamingDispatcherService();
      delegateLogStreamingDispatcherService.startAsync();
      this.svcHolder.set(delegateLogStreamingDispatcherService);
    }
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void stop() {
    if (running.compareAndSet(true, false)) {
      DelegateLogStreamingDispatcherService delegateLogStreamingDispatcherService = this.svcHolder.get();
      delegateLogStreamingDispatcherService.stopAsync().awaitTerminated();
    }
  }
}
