/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.cvng.beans;

import io.harness.cvng.utils.CloudWatchUtils;
import io.harness.delegate.beans.connector.awsconnector.AwsConnectorDTO;

import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CloudWatchMetricDataCollectionInfo extends TimeSeriesDataCollectionInfo<AwsConnectorDTO> {
  private static final String QUERIES_KEY = "queries";
  private static final String METRIC_IDENTIFIERS_KEY = "metricIdentifiers";
  private static final String JSON_PATH_KEY = "jsonPaths";
  private static final String METRIC_JSON_PATH_KEY = "metricJsonPaths";
  private static final String TIMESTAMP_JSON_PATH_KEY = "timestampJsonPaths";
  private static final String HOST_JSON_PATH_KEY = "hostJsonPaths";
  private static final String METRIC_NAMES_KEY = "metricNames";

  private String region;
  private List<CloudWatchMetricInfoDTO> metricInfoList;
  private MetricPackDTO metricPack;

  public List<CloudWatchMetricInfoDTO> getMetricInfoList() {
    if (metricInfoList == null) {
      return new ArrayList<>();
    }
    return metricInfoList;
  }

  boolean customQuery;

  @Override
  public Map<String, Object> getDslEnvVariables(AwsConnectorDTO awsConnectorDTO) {
    // Todo: All queries are custom queries for now. Add check when we support other queries.
//    return getEnvVariablesForCustomConfig();
    return null;
  }

//  private Map<String, Object> getEnvVariablesForCustomConfig() {
//    Map<String, Object> dslEnvVariables = new HashMap<>();
//    dslEnvVariables.put("region", region);
//    dslEnvVariables.put("collectHostData", Boolean.toString(this.isCollectHostData()));
//    dslEnvVariables.put("groupName", groupName);
//    List<String> listOfQueries =
//        getMetricInfoList().stream().map(CloudWatchMetricInfoDTO::getExpression).collect(Collectors.toList());
//    List<String> metricNames =
//        getMetricInfoList().stream().map(CloudWatchMetricInfoDTO::getMetricName).collect(Collectors.toList());
//    List<String> metricValuePaths = getMetricInfoList()
//                                        .stream()
//                                        .map(infoDto -> infoDto.getResponseMapping().getMetricValueJsonPath())
//                                        .collect(Collectors.toList());
//    List<String> timestampPaths = getMetricInfoList()
//                                      .stream()
//                                      .map(infoDto -> infoDto.getResponseMapping().getTimestampJsonPath())
//                                      .collect(Collectors.toList());
//
//    List<String> hostJsonPaths = getMetricInfoList()
//                                     .stream()
//                                     .map(infoDto -> infoDto.getResponseMapping().getServiceInstanceJsonPath())
//                                     .collect(Collectors.toList());
//
//    List<String> metricIdentifiers =
//        getMetricInfoList().stream().map(infoDto -> infoDto.getMetricIdentifier()).collect(Collectors.toList());
//
//    dslEnvVariables.put(QUERIES_KEY, listOfQueries);
//    dslEnvVariables.put(METRIC_IDENTIFIERS_KEY, metricIdentifiers);
//    dslEnvVariables.put(METRIC_JSON_PATH_KEY, metricValuePaths);
//    dslEnvVariables.put(TIMESTAMP_JSON_PATH_KEY, timestampPaths);
//    dslEnvVariables.put(METRIC_NAMES_KEY, metricNames);
//    dslEnvVariables.put(HOST_JSON_PATH_KEY, hostJsonPaths);
//
//    return dslEnvVariables;
//  }

  @Override
  public String getBaseUrl(AwsConnectorDTO awsConnectorDTO) {
    return CloudWatchUtils.getBaseUrl(region, "monitoring");
  }

  @Override
  public Map<String, String> collectionHeaders(AwsConnectorDTO awsConnectorDTO) {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, String> collectionParams(AwsConnectorDTO awsConnectorDTO) {
    return Collections.emptyMap();
  }

  @Data
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class CloudWatchMetricInfoDTO {
    String expression;
    String metricName;
    String metricIdentifier;
    private String groupName;
    MetricResponseMappingDTO responseMapping;
  }
}
