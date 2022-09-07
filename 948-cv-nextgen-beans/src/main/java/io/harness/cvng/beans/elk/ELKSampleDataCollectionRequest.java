/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.beans.elk;

import static io.harness.annotations.dev.HarnessTeam.CV;

import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.beans.DataCollectionRequestType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeName("ELK_SAMPLE_DATA")
@SuperBuilder
@NoArgsConstructor
@FieldNameConstants(innerTypeName = "ELKSampleDataCollectionRequestKeys")
@OwnedBy(CV)
public class ELKSampleDataCollectionRequest extends ELKDataCollectionRequest {
  Long from;
  Long to;
  String timestampTag;
  String timestampFormat;
  String index;
  String query;
  Long limit;
  @Override
  public String getDSL() {
    return ELKSampleDataCollectionRequest.readDSL(
        "elk-sample-data.datacollection", ELKSampleDataCollectionRequest.class);
  }

  @Override
  public Map<String, Object> fetchDslEnvVariables() {
    Map<String, Object> dslEnvVariables = super.fetchDslEnvVariables();
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.query, query);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.from, from);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.to, to);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.limit, limit);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.index, index);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.timestampTag, timestampTag);
    dslEnvVariables.put(ELKSampleDataCollectionRequestKeys.timestampFormat, timestampFormat);
    return dslEnvVariables;
  }

  @Override
  public DataCollectionRequestType getType() {
    return DataCollectionRequestType.ELK_SAMPLE_DATA;
  }
}
