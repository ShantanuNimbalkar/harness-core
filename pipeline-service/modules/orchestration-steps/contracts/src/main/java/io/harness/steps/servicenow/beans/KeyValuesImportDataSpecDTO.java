/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.steps.servicenow.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.yaml.ParameterField;
import io.harness.steps.servicenow.ServiceNowStepUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@OwnedBy(CDC)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ApiModel("keyValuesImportDataSpec")
@Schema(name = "KeyValuesImportDataSpec", description = "This contains details of Key-Value Import Data specifications")
public class KeyValuesImportDataSpecDTO implements ImportDataSpecDTO {
  @NotNull Map<String, String> importDataFieldsMap;

  public static KeyValuesImportDataSpecDTO fromKeyValuesImportDataSpec(KeyValuesImportDataSpec keyValuesCriteriaSpec) {
    Map<String, ParameterField<String>> parameterizedFieldsMap =
        ServiceNowStepUtils.processServiceNowFieldsList(keyValuesCriteriaSpec.getFields());
    return KeyValuesImportDataSpecDTO.builder()
        .importDataFieldsMap(ServiceNowStepUtils.processServiceNowFieldsInSpec(parameterizedFieldsMap))
        .build();
  }
}
