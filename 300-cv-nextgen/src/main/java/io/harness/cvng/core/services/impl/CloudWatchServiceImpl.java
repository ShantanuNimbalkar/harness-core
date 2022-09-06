/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.services.impl;

import io.harness.cvng.beans.DataCollectionRequest;
import io.harness.cvng.beans.DataCollectionRequestType;
import io.harness.cvng.beans.cloudwatch.CloudWatchMetricFetchSampleDataRequest;
import io.harness.cvng.core.beans.OnboardingRequestDTO;
import io.harness.cvng.core.beans.OnboardingResponseDTO;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.services.CloudWatchService;
import io.harness.cvng.core.services.api.OnboardingService;
import io.harness.datacollection.exception.DataCollectionException;
import io.harness.serializer.JsonUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloudWatchServiceImpl implements CloudWatchService {
  @Inject private OnboardingService onboardingService;

  @Override
  public LinkedHashMap fetchSampleData(
      ProjectParams projectParams, String connectorIdentifier, String query, String tracingId) {
    try {
      // TODO: Add validations for cloudwatch config
      //      Preconditions.checkNotNull(query);
      //      query = query.trim();
      //      Preconditions.checkState(!query.contains(" SINCE "),
      //          "Query should not contain any time duration. Please remove SINCE or any time related keywords");
      //      Preconditions.checkState(query.endsWith("TIMESERIES"), "Query should end with the TIMESERIES keyword");

      DataCollectionRequest request = CloudWatchMetricFetchSampleDataRequest.builder()
                                          .type(DataCollectionRequestType.CLOUDWATCH_METRIC_SAMPLE_DATA_REQUEST)
                                          .query(query)
                                          .build();
      OnboardingRequestDTO onboardingRequestDTO = OnboardingRequestDTO.builder()
                                                      .dataCollectionRequest(request)
                                                      .connectorIdentifier(connectorIdentifier)
                                                      .accountId(projectParams.getAccountIdentifier())
                                                      .orgIdentifier(projectParams.getOrgIdentifier())
                                                      .tracingId(tracingId)
                                                      .projectIdentifier(projectParams.getProjectIdentifier())
                                                      .build();

      OnboardingResponseDTO response =
          onboardingService.getOnboardingResponse(projectParams.getAccountIdentifier(), onboardingRequestDTO);

      final Gson gson = new Gson();
      Type type = new TypeToken<LinkedHashMap>() {}.getType();
      return gson.fromJson(JsonUtils.asJson(response.getResult()), type);
    } catch (DataCollectionException ex) {
      return null;
    }
  }
}
