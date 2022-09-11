/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.beans.monitoredService.healthSouceSpec;

import io.harness.cvng.beans.CVMonitoringCategory;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.core.beans.HealthSourceMetricDefinition;
import io.harness.cvng.core.beans.monitoredService.HealthSource;
import io.harness.cvng.core.beans.monitoredService.TimeSeriesMetricPackDTO;
import io.harness.cvng.core.constant.MonitoredServiceConstants;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.CloudWatchMetricCVConfig;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.services.api.MetricPackService;
import io.harness.cvng.core.validators.UniqueIdentifierCheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudWatchMetricsHealthSourceSpec extends MetricHealthSourceSpec {
  @NotNull private String region;
  @NotNull String feature;
  @Valid Set<TimeSeriesMetricPackDTO> metricPacks;
  @Valid @UniqueIdentifierCheck List<CloudWatchMetricDefinition> metricDefinitions;

  public List<CloudWatchMetricDefinition> getMetricDefinitions() {
    return CollectionUtils.isEmpty(metricDefinitions) ? Collections.emptyList() : metricDefinitions;
  }

  @Override
  public void validate() {
    getMetricDefinitions().forEach(metricDefinition
        -> Preconditions.checkArgument(
            !(Objects.nonNull(metricDefinition.getAnalysis())
                && Objects.nonNull(metricDefinition.getAnalysis().getDeploymentVerification())
                && Objects.nonNull(metricDefinition.getAnalysis().getDeploymentVerification().getEnabled())
                && metricDefinition.getAnalysis().getDeploymentVerification().getEnabled()
                && StringUtils.isEmpty(metricDefinition.getResponseMapping().getServiceInstanceJsonPath())),
            "Service instance label/key/path shouldn't be empty for Deployment Verification"));
  }

  @Override
  public HealthSource.CVConfigUpdateResult getCVConfigUpdateResult(String accountId, String orgIdentifier,
      String projectIdentifier, String environmentRef, String serviceRef, String monitoredServiceIdentifier,
      String identifier, String name, List<CVConfig> existingCVConfigs, MetricPackService metricPackService) {
    List<CloudWatchMetricCVConfig> cvConfigsFromThisObj = toCVConfigs(accountId, orgIdentifier, projectIdentifier,
        environmentRef, serviceRef, monitoredServiceIdentifier, identifier, name, metricPackService);
    Map<Key, CloudWatchMetricCVConfig> existingConfigMap = new HashMap<>();
    List<CloudWatchMetricCVConfig> existingCloudWatchCVConfigs =
        (List<CloudWatchMetricCVConfig>) (List<?>) existingCVConfigs;
    for (CloudWatchMetricCVConfig cloudWatchMetricCVConfig : existingCloudWatchCVConfigs) {
      existingConfigMap.put(getKeyFromCVConfig(cloudWatchMetricCVConfig), cloudWatchMetricCVConfig);
    }
    Map<Key, CloudWatchMetricCVConfig> currentCVConfigsMap = new HashMap<>();
    for (CloudWatchMetricCVConfig cloudWatchMetricCVConfig : cvConfigsFromThisObj) {
      currentCVConfigsMap.put(getKeyFromCVConfig(cloudWatchMetricCVConfig), cloudWatchMetricCVConfig);
    }
    Set<Key> deleted = Sets.difference(existingConfigMap.keySet(), currentCVConfigsMap.keySet());
    Set<Key> added = Sets.difference(currentCVConfigsMap.keySet(), existingConfigMap.keySet());
    Set<Key> updated = Sets.intersection(existingConfigMap.keySet(), currentCVConfigsMap.keySet());
    List<CVConfig> updatedConfigs =
        updated.stream().map(key -> currentCVConfigsMap.get(key)).collect(Collectors.toList());
    List<CVConfig> updatedConfigWithUuid =
        updated.stream().map(key -> existingConfigMap.get(key)).collect(Collectors.toList());
    for (int i = 0; i < updatedConfigs.size(); i++) {
      updatedConfigs.get(i).setUuid(updatedConfigWithUuid.get(i).getUuid());
    }
    return HealthSource.CVConfigUpdateResult.builder()
        .deleted(deleted.stream().map(key -> existingConfigMap.get(key)).collect(Collectors.toList()))
        .updated(updatedConfigs)
        .added(added.stream().map(key -> currentCVConfigsMap.get(key)).collect(Collectors.toList()))
        .build();
  }

  @Override
  public DataSourceType getType() {
    return DataSourceType.APP_DYNAMICS;
  }

  private List<CloudWatchMetricCVConfig> toCVConfigs(String accountId, String orgIdentifier, String projectIdentifier,
      String environmentRef, String serviceRef, String monitoredServiceIdentifier, String identifier, String name,
      MetricPackService metricPackService) {
    List<CloudWatchMetricCVConfig> cvConfigs = new ArrayList<>();
    CollectionUtils.emptyIfNull(metricPacks)
        .stream()
        .filter(
            metricPack -> !metricPack.getIdentifier().equalsIgnoreCase(MonitoredServiceConstants.CUSTOM_METRIC_PACK))
        .forEach(metricPack -> {
          MetricPack metricPackFromDb =
              metricPack.toMetricPack(accountId, orgIdentifier, projectIdentifier, getType(), metricPackService);
          CloudWatchMetricCVConfig cloudWatchMetricCVConfig =
              CloudWatchMetricCVConfig.builder()
                  .accountId(accountId)
                  .orgIdentifier(orgIdentifier)
                  .projectIdentifier(projectIdentifier)
                  .identifier(identifier)
                  .connectorIdentifier(getConnectorRef())
                  .monitoringSourceName(name)
                  .monitoredServiceIdentifier(monitoredServiceIdentifier)
                  .productName(feature)
                  .region(region)
                  .metricPack(metricPackFromDb)
                  .category(metricPackFromDb.getCategory())
                  .build();
          cvConfigs.add(cloudWatchMetricCVConfig);
        });
    cvConfigs.addAll(CollectionUtils.emptyIfNull(metricDefinitions)
                         .stream()
                         .collect(Collectors.groupingBy(MetricDefinitionKey::fromMetricDefinition))
                         .values()
                         .stream()
                         .map(mdList -> {
                           CloudWatchMetricCVConfig cloudWatchMetricCVConfig =
                               CloudWatchMetricCVConfig.builder()
                                   .accountId(accountId)
                                   .orgIdentifier(orgIdentifier)
                                   .projectIdentifier(projectIdentifier)
                                   .identifier(identifier)
                                   .connectorIdentifier(getConnectorRef())
                                   .monitoringSourceName(name)
                                   .productName(feature)
                                   .region(region)
                                   .monitoredServiceIdentifier(monitoredServiceIdentifier)
                                   .groupName(mdList.get(0).getGroupName())
                                   .category(mdList.get(0).getRiskProfile().getCategory())
                                   .build();
                           cloudWatchMetricCVConfig.populateFromMetricDefinitions(
                               metricDefinitions, metricDefinitions.get(0).getRiskProfile().getCategory());
                           return cloudWatchMetricCVConfig;
                         })
                         .collect(Collectors.toList()));

    cvConfigs.forEach(cvConfig -> cvConfig.addMetricThresholds(metricPacks));
    cvConfigs.stream()
        .filter(cvConfig -> CollectionUtils.isNotEmpty(cvConfig.getMetricInfos()))
        .flatMap(cvConfig -> cvConfig.getMetricInfos().stream())
        .forEach(metricInfo -> {
          if (metricInfo.getDeploymentVerification().isEnabled()) {
            Preconditions.checkNotNull(metricInfo.getResponseMapping().getServiceInstanceJsonPath(),
                "ServiceInstanceJsonPath should be set for Deployment Verification");
          }
        });
    return cvConfigs;
  }

  private Key getKeyFromCVConfig(CloudWatchMetricCVConfig cloudWatchMetricCVConfig) {
    return Key.builder()
        .region(cloudWatchMetricCVConfig.getRegion())
        .metricPack(cloudWatchMetricCVConfig.getMetricPack())
        .build();
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class CloudWatchMetricDefinition extends HealthSourceMetricDefinition {
    String groupName;
    String expression;
    MetricResponseMapping responseMapping;
  }

  @Value
  @Builder
  private static class Key {
    String region;
    MetricPack metricPack;
  }

  @Value
  @Builder
  private static class MetricDefinitionKey {
    String groupName;
    CVMonitoringCategory category;

    public static MetricDefinitionKey fromMetricDefinition(CloudWatchMetricDefinition cloudWatchMetricDefinition) {
      return MetricDefinitionKey.builder()
          .category(cloudWatchMetricDefinition.getRiskProfile().getCategory())
          .groupName(cloudWatchMetricDefinition.getGroupName())
          .build();
    }
  }
}
