/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.steps.servicenow.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import static software.wings.utils.Utils.isJSONValid;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.yaml.ParameterField;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@OwnedBy(CDC)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ApiModel("JsonImportDataSpec")
@Schema(name = "JsonImportDataSpec", description = "his contains details of Json Import Data specifications")
public class JsonImportDataSpecDTO implements ImportDataSpecDTO {
  @NotNull Map importDataFieldsMap;

  public static JsonImportDataSpecDTO fromJsonImportDataSpec(JsonImportDataSpec jsonImportDataSpec) {
    if (ParameterField.isNull(jsonImportDataSpec.getJsonBody())) {
      throw new InvalidRequestException("Json Body can't be null");
    }
    String jsonBodyString = (String) jsonImportDataSpec.getJsonBody().fetchFinalValue();
    if (!isJSONValid(jsonBodyString)) {
      throw new InvalidRequestException(String.format("Provided Json Body : [%s] is not a valid Json", jsonBodyString));
    }
    if (StringUtils.isBlank(jsonBodyString)) {
      // empty json is also allowed in import sets
      return JsonImportDataSpecDTO.builder().importDataFieldsMap(new HashMap<>()).build();
    }
    HashMap jsonBodyMap = null;
    try {
      Gson gson = new Gson();
      jsonBodyMap = gson.fromJson(jsonBodyString, HashMap.class);
    } catch (JsonSyntaxException ex) {
      throw new InvalidRequestException(String.format("Provided Json Body is not a valid Json: %s", ex.getMessage()));
    }
    return JsonImportDataSpecDTO.builder().importDataFieldsMap(jsonBodyMap).build();
  }
}
