/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.logstreaming;

import static software.wings.beans.LogColor.Red;
import static software.wings.beans.LogColor.Yellow;
import static software.wings.beans.LogHelper.COMMAND_UNIT_PLACEHOLDER;
import static software.wings.beans.LogHelper.color;
import static software.wings.beans.LogHelper.doneColoring;
import static software.wings.beans.LogWeight.Bold;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.beans.taskprogress.ITaskProgressClient;
import io.harness.exception.InvalidArgumentsException;
import io.harness.logging.LogCallback;
import io.harness.logging.LogLevel;
import io.harness.network.SafeHttpCall;

import software.wings.beans.command.ExecutionLogCallback;
import software.wings.delegatetasks.DelegateLogService;

import java.util.concurrent.ExecutorService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * There are certain assumptions for this client to work
 * 1. Each delegate task creates a separate task client
 * 2. the usage of this client is
 *    -> open stream
 *    -> write line
 *    -> close stream
 * concurrent usage of open and close stream will result in loss of logs
 */
@Builder
@Slf4j
@TargetModule(HarnessModule._420_DELEGATE_AGENT)
public class LogStreamingTaskClient implements ILogStreamingTaskClient {
  private final DelegateLogService logService;
  private final LogStreamingClient logStreamingClient;
  private final LogStreamingSanitizer logStreamingSanitizer;
  private final ExecutorService taskProgressExecutor;

  private final DelegateLogStreamingDispatcher delegateLogStreamingDispatcher;
  private final String token;
  private final String accountId;
  private final String baseLogKey;
  @Deprecated private final String appId;
  @Deprecated private final String activityId;

  private final ITaskProgressClient taskProgressClient;

  @Override
  public void openStream(String baseLogKeySuffix) {
    String logKey = getLogKey(baseLogKeySuffix);

    try {
      SafeHttpCall.executeWithExceptions(logStreamingClient.openLogStream(token, accountId, logKey));
    } catch (Exception ex) {
      log.error("Unable to open log stream for account {} and key {}", accountId, logKey, ex);
    }
  }

  @Override
  public void closeStream(String baseLogKeySuffix) {
    String logKey = getLogKey(baseLogKeySuffix);

    // we don't want workflow steps to hang because of any log reasons. Putting a safety net just in case
    //    if (delegateLogStreamingDispatcher.dispatchAllLogsBeforeClosingStream(logKey)) {
    //      log.debug("for {} the logs are not drained yet. sleeping for 100ms...", logKey);
    //      // Question? how to sleep it for 100 ms
    //    }

    try {
      SafeHttpCall.executeWithExceptions(logStreamingClient.closeLogStream(token, accountId, logKey, true));
    } catch (Exception ex) {
      log.error("Unable to close log stream for account {} and key {}", accountId, logKey, ex);
    }
  }

  @Override
  public void writeLogLine(LogLine logLine, String baseLogKeySuffix) {
    if (logLine == null) {
      throw new InvalidArgumentsException("Log line parameter is mandatory.");
    }

    String logKey = getLogKey(baseLogKeySuffix);

    logStreamingSanitizer.sanitizeLogMessage(logLine);
    colorLog(logLine);

    delegateLogStreamingDispatcher.saveLogsInCache(logKey, logLine);
  }

  @Override
  public void dispatchLogs() {
    delegateLogStreamingDispatcher.swapMapsAndDispatchLogs();
  }

  @NotNull
  private String getLogKey(String baseLogKeySuffix) {
    return baseLogKey + (isBlank(baseLogKeySuffix) ? "" : String.format(COMMAND_UNIT_PLACEHOLDER, baseLogKeySuffix));
  }

  private void colorLog(LogLine logLine) {
    String message = logLine.getMessage();
    if (logLine.getLevel() == LogLevel.ERROR) {
      message = color(message, Red, Bold);
    } else if (logLine.getLevel() == LogLevel.WARN) {
      message = color(message, Yellow, Bold);
    }
    message = doneColoring(message);
    logLine.setMessage(message);
  }

  @Override
  public LogCallback obtainLogCallback(String commandName) {
    if (isBlank(appId) || isBlank(activityId)) {
      throw new InvalidArgumentsException(
          "Application id and activity id were not available as part of task params. Please make sure that task params class implements Cd1ApplicationAccess and ActivityAccess interfaces.");
    }

    return new ExecutionLogCallback(logService, accountId, appId, activityId, commandName);
  }

  @Override
  public ITaskProgressClient obtainTaskProgressClient() {
    return taskProgressClient;
  }

  @Override
  public ExecutorService obtainTaskProgressExecutor() {
    return taskProgressExecutor;
  }
}
