/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.services.impl;

import io.harness.cvng.core.beans.TimeSeriesSampleDTO;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.services.api.ELKService;

import java.util.LinkedHashMap;
import java.util.List;

public class ELKServiceImpl implements ELKService {
  @Override
  public void checkConnectivity(
      String accountId, String orgIdentifier, String projectIdentifier, String connectorIdentifier, String tracingId) {}

  @Override
  public List<String> getLogIndexes(ProjectParams projectParams, String connectorIdentifier, String tracingId) {
    return null;
  }

  @Override
  public List<LinkedHashMap> getSampleLogData(
      ProjectParams projectParams, String connectorIdentifier, String query, String tracingId) {
    return null;
  }

  @Override
  public List<TimeSeriesSampleDTO> getMetricSampleLogData(
      ProjectParams projectParams, String connectorIdentifier, String query, String tracingId) {
    return null;
  }
}
