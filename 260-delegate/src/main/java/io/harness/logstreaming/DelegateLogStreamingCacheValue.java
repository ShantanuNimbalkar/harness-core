package io.harness.logstreaming;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@OwnedBy(HarnessTeam.DEL)
public class DelegateLogStreamingCacheValue {
  private List<LogLine> logLines = new ArrayList<>();
  private boolean shouldCloseStream = false;
}
