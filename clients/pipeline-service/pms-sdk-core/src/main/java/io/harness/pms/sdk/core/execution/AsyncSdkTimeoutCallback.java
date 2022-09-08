/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.sdk.core.execution;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.tasks.ResponseData;
import io.harness.waiter.OldNotifyCallback;

import com.google.inject.Inject;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@OwnedBy(PIPELINE)
@Slf4j
@Builder
public class AsyncSdkTimeoutCallback implements OldNotifyCallback {
  @Inject SdkNodeExecutionService sdkNodeExecutionService;
  byte[] ambianceBytes;

  @Override
  public void notifyTimeout(Map<String, ResponseData> responseMap) {
    notifyResponse(responseMap, true);
  }

  @Override
  public void notify(Map<String, ResponseData> response) {
    notifyResponse(response, false);
  }

  @Override
  public void notifyError(Map<String, ResponseData> response) {
    notifyResponse(response, false);
  }

  // TODO: Implement the methods.
  private void notifyResponse(Map<String, ResponseData> responseMap, boolean timedOut) {
    Ambiance ambiance = Ambiance.parseFrom(ambianceBytes);
    sdkNodeExecutionService.resumeNodeExecution(ambiance, responseMap, false, timedOut);
  }
}
