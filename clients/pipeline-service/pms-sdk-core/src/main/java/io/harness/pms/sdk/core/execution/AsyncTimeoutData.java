package io.harness.pms.sdk.core.execution;

import io.harness.tasks.ResponseData;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AsyncTimeoutData implements ResponseData {
  boolean timedOut;
}
