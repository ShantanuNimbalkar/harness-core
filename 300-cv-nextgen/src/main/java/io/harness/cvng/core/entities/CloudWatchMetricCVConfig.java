/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.entities;
import static io.harness.cvng.core.utils.ErrorMessageUtils.generateErrorMessageFromParam;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import static com.google.common.base.Preconditions.checkNotNull;

import io.harness.cvng.beans.CVMonitoringCategory;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.beans.ThresholdConfigType;
import io.harness.cvng.beans.TimeSeriesMetricType;
import io.harness.cvng.core.beans.HealthSourceQueryType;
import io.harness.cvng.core.beans.monitoredService.TimeSeriesMetricPackDTO;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.CloudWatchMetricsHealthSourceSpec;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.MetricResponseMapping;
import io.harness.cvng.core.constant.MonitoredServiceConstants;
import io.harness.cvng.core.entities.CloudWatchMetricCVConfig.CloudWatchMetricInfo;
import io.harness.cvng.core.services.CVNextGenConstants;
import io.harness.cvng.core.utils.analysisinfo.AnalysisInfoUtility;
import io.harness.cvng.core.utils.analysisinfo.DevelopmentVerificationTransformer;
import io.harness.cvng.core.utils.analysisinfo.LiveMonitoringTransformer;
import io.harness.cvng.core.utils.analysisinfo.SLIMetricTransformer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.mongodb.morphia.query.UpdateOperations;

@JsonTypeName("CLOUDWATCH_METRICS")
@Data
@SuperBuilder
@FieldNameConstants(innerTypeName = "CloudWatchMetricCVConfigKeys")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CloudWatchMetricCVConfig extends MetricCVConfig<CloudWatchMetricInfo> {
  private String region;
  private String groupName;
  private List<CloudWatchMetricInfo> metricInfos;
  private HealthSourceQueryType queryType;

  public boolean isCustomQuery() {
    return true;
  }

  @Override
  public DataSourceType getType() {
    return DataSourceType.CLOUDWATCH_METRICS;
  }

  @Override
  @JsonIgnore
  public String getDataCollectionDsl() {
    return getMetricPack().getDataCollectionDsl();
  }

  @Override
  protected void validateParams() {
    boolean regionPresent = false, customMetricPresent = false;
    if (CollectionUtils.isEmpty(metricInfos)) {
      checkNotNull(region, generateErrorMessageFromParam(CloudWatchMetricCVConfigKeys.region));
      regionPresent = true;
    } else {
      customMetricPresent = true;
    }
    Preconditions.checkState(regionPresent || customMetricPresent,
        "CVConfig should have either application based setup or custom metric setup or both.");
  }

  @Override
  public boolean isSLIEnabled() {
    if (!getMetricPack().getIdentifier().equals(CVNextGenConstants.CUSTOM_PACK_IDENTIFIER)) {
      return false;
    }
    return AnalysisInfoUtility.anySLIEnabled(metricInfos);
  }

  @Override
  public boolean isLiveMonitoringEnabled() {
    if (!getMetricPack().getIdentifier().equals(CVNextGenConstants.CUSTOM_PACK_IDENTIFIER)) {
      return true;
    }
    return AnalysisInfoUtility.anyLiveMonitoringEnabled(metricInfos);
  }

  @Override
  public boolean isDeploymentVerificationEnabled() {
    if (!getMetricPack().getIdentifier().equals(CVNextGenConstants.CUSTOM_PACK_IDENTIFIER)) {
      return true;
    }
    return AnalysisInfoUtility.anyDeploymentVerificationEnabled(metricInfos);
  }

  @Override
  public Optional<String> maybeGetGroupName() {
    return Optional.ofNullable(groupName);
  }

  @Override
  public List<CloudWatchMetricInfo> getMetricInfos() {
    if (metricInfos == null) {
      return Collections.emptyList();
    }
    return metricInfos;
  }

  public static class CloudWatchMetricCVConfigUpdatableEntity
      extends MetricCVConfigUpdatableEntity<CloudWatchMetricCVConfig, CloudWatchMetricCVConfig> {
    @Override
    public void setUpdateOperations(UpdateOperations<CloudWatchMetricCVConfig> updateOperations,
        CloudWatchMetricCVConfig cloudWatchMetricCVConfig) {
      setCommonOperations(updateOperations, cloudWatchMetricCVConfig);
      updateOperations.set(CloudWatchMetricCVConfigKeys.region, cloudWatchMetricCVConfig.getRegion());
      if (cloudWatchMetricCVConfig.getMetricInfos() != null) {
        updateOperations.set(CloudWatchMetricCVConfigKeys.metricInfos, cloudWatchMetricCVConfig.getMetricInfos());
      }
    }
  }

  public void populateFromMetricDefinitions(
      List<CloudWatchMetricsHealthSourceSpec.CloudWatchMetricDefinition> metricDefinitions,
      CVMonitoringCategory category) {
    if (this.metricInfos == null) {
      this.metricInfos = new ArrayList<>();
    }
    MetricPack metricPack = MetricPack.builder()
                                .category(category)
                                .accountId(getAccountId())
                                .dataSourceType(DataSourceType.CLOUDWATCH_METRICS)
                                .projectIdentifier(getProjectIdentifier())
                                .orgIdentifier(getOrgIdentifier())
                                .identifier(CVNextGenConstants.CUSTOM_PACK_IDENTIFIER)
                                .category(category)
                                .build();

    metricDefinitions.stream().filter(md -> md.getGroupName().equals(getGroupName())).forEach(md -> {
      CloudWatchMetricInfo info =
          CloudWatchMetricInfo.builder()
              .identifier(md.getIdentifier())
              .metricName(md.getMetricName())
              .expression(md.getExpression())
              .responseMapping(md.getResponseMapping())
              .sli(SLIMetricTransformer.transformDTOtoEntity(md.getSli()))
              .liveMonitoring(LiveMonitoringTransformer.transformDTOtoEntity(md.getAnalysis()))
              .deploymentVerification(DevelopmentVerificationTransformer.transformDTOtoEntity(md.getAnalysis()))
              .metricType(md.getRiskProfile().getMetricType())
              .build();
      this.metricInfos.add(info);
      Set<TimeSeriesThreshold> thresholds = getThresholdsToCreateOnSaveForCustomProviders(
          info.getMetricName(), info.getMetricType(), md.getRiskProfile().getThresholdTypes());

      metricPack.addToMetrics(MetricPack.MetricDefinition.builder()
                                  .thresholds(new ArrayList<>(thresholds))
                                  .type(info.getMetricType())
                                  .name(info.getMetricName())
                                  .identifier(info.getIdentifier())
                                  .included(true)
                                  .build());
    });

    this.setMetricPack(metricPack);
  }

  public void addMetricThresholds(Set<TimeSeriesMetricPackDTO> timeSeriesMetricPacks) {
    if (isEmpty(timeSeriesMetricPacks)) {
      return;
    }
    getMetricPack().getMetrics().forEach(metric
        -> timeSeriesMetricPacks.stream()
               .filter(timeSeriesMetricPack
                   -> timeSeriesMetricPack.getIdentifier().equalsIgnoreCase(
                       MonitoredServiceConstants.CUSTOM_METRIC_PACK))
               .forEach(timeSeriesMetricPackDTO -> {
                 if (!isEmpty(timeSeriesMetricPackDTO.getMetricThresholds())) {
                   timeSeriesMetricPackDTO.getMetricThresholds()
                       .stream()
                       .filter(metricPackDTO -> metric.getName().equals(metricPackDTO.getMetricName()))
                       .forEach(metricPackDTO -> metricPackDTO.getTimeSeriesThresholdCriteria().forEach(criteria -> {
                         List<TimeSeriesThreshold> timeSeriesThresholds =
                             metric.getThresholds() != null ? metric.getThresholds() : new ArrayList<>();
                         TimeSeriesThreshold timeSeriesThreshold =
                             TimeSeriesThreshold.builder()
                                 .accountId(getAccountId())
                                 .projectIdentifier(getProjectIdentifier())
                                 .dataSourceType(getType())
                                 .metricIdentifier(metric.getIdentifier())
                                 .metricType(metric.getType())
                                 .metricName(metricPackDTO.getMetricName())
                                 .action(metricPackDTO.getType().getTimeSeriesThresholdActionType())
                                 .criteria(criteria)
                                 .thresholdConfigType(ThresholdConfigType.CUSTOMER)
                                 .build();
                         timeSeriesThresholds.add(timeSeriesThreshold);
                         metric.setThresholds(timeSeriesThresholds);
                       }));
                 }
               }));
  }

  @Value
  @SuperBuilder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @EqualsAndHashCode(callSuper = true)
  public static class CloudWatchMetricInfo extends AnalysisInfo {
    String expression;
    TimeSeriesMetricType metricType;
    MetricResponseMapping responseMapping;
  }
}
