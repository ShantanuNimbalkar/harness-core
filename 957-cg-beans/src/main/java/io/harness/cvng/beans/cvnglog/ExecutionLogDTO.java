/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.beans.cvnglog;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("ExecutionLog")
@NoArgsConstructor
@OwnedBy(HarnessTeam.CV)
public class ExecutionLogDTO extends CVNGLogDTO {
  private String log;
  private LogLevel logLevel;

  @Override
  public CVNGLogType getType() {
    return CVNGLogType.EXECUTION_LOG;
  }

  public enum LogLevel { INFO, WARN, ERROR }
}
