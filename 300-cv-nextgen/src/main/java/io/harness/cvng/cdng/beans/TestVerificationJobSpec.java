/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.cdng.beans;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.verificationjob.entities.TestVerificationJob;
import io.harness.cvng.verificationjob.entities.VerificationJob.RuntimeParameter;
import io.harness.cvng.verificationjob.entities.VerificationJob.VerificationJobBuilder;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeName("LoadTest")
@OwnedBy(HarnessTeam.CV)
@SuperBuilder
@NoArgsConstructor
public class TestVerificationJobSpec extends VerificationJobSpec {
  @Override
  public String getType() {
    return "LoadTest";
  }

  @Override
  public VerificationJobBuilder verificationJobBuilder() {
    return TestVerificationJob.builder().sensitivity(
        RuntimeParameter.builder().isRuntimeParam(false).value(getSensitivity().getValue()).build());
  }

  @Override
  protected void validateParams() {}
}
