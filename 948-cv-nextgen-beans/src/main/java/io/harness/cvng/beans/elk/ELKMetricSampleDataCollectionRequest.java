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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeName("ELK_METRIC_SAMPLE_DATA")
@SuperBuilder
@NoArgsConstructor
@FieldNameConstants(innerTypeName = "ELKMetricSampleDataCollectionRequestKeys")
@OwnedBy(CV)
public class ELKMetricSampleDataCollectionRequest extends ELKDataCollectionRequest {
  @Override
  public String getDSL() {
    return ELKMetricSampleDataCollectionRequest.readDSL(
        "elk-metric-sample-data.datacollection", ELKMetricSampleDataCollectionRequest.class);
  }

  @Override
  public DataCollectionRequestType getType() {
    return DataCollectionRequestType.ELK_METRIC_SAMPLE_DATA;
  }
}
