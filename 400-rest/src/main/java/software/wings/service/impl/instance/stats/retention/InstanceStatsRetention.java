/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.service.impl.instance.stats.retention;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.timescaledb.retention.RetentionManager;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@OwnedBy(HarnessTeam.CDP)
public class InstanceStatsRetention {
  private RetentionManager retentionManager;
  private static final String INSTANCE_STATS = "instance_stats";
  private static final String INSTANCE_STATS_DAY = "instance_stats_day";
  private static final String INSTANCE_STATS_HOUR = "instance_stats_hour";
  private static final String RETENTION_PERIOD = "7 months";

  public void addRetentionPolicy() {
    retentionManager.addPolicy(INSTANCE_STATS, RETENTION_PERIOD);
    retentionManager.addPolicy(INSTANCE_STATS_DAY, RETENTION_PERIOD);
    retentionManager.addPolicy(INSTANCE_STATS_HOUR, RETENTION_PERIOD);
  }
}
