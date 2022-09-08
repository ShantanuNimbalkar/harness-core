/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.provision.azure;

import static io.harness.rule.OwnerRule.NGONZALEZ;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.harness.CategoryTest;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.yaml.ParameterField;
import io.harness.rule.Owner;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(HarnessTeam.CDP)
public class AzureARMRollbackStepInfoTest extends CategoryTest {
  @Test
  @Owner(developers = NGONZALEZ)
  @Category(UnitTests.class)
  public void testValidateParamsWithNoDeploymentInfo() {
    AzureARMRollbackStepInfo rollbackStepInfo = new AzureARMRollbackStepInfo();
    assertThatThrownBy(rollbackStepInfo::validateSpecParameters)
        .hasMessageContaining("The provisioner Identifier can't be empty");
  }

  @Test
  @Owner(developers = NGONZALEZ)
  @Category(UnitTests.class)
  public void testValidateParams() {
    AzureARMRollbackStepInfo rollbackStep = new AzureARMRollbackStepInfo();
    rollbackStep.setProvisionerIdentifier(ParameterField.<String>builder().value("foobar").build());
    assertThatCode(rollbackStep::validateSpecParameters).doesNotThrowAnyException();
  }
}
