/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.beans.monitoredService.healthSourceSpec;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.DHRUVX;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CvNextGenTestBase;
import io.harness.category.element.UnitTests;
import io.harness.cvng.BuilderFactory;
import io.harness.cvng.beans.CVMonitoringCategory;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.core.beans.monitoredService.HealthSource.CVConfigUpdateResult;
import io.harness.cvng.core.beans.monitoredService.TimeSeriesMetricPackDTO;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.CloudWatchMetricsHealthSourceSpec;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.CloudWatchMetricCVConfig;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.services.CVNextGenConstants;
import io.harness.cvng.core.services.api.MetricPackService;
import io.harness.rule.Owner;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudWatchMetricsHealthSourceSpecTest extends CvNextGenTestBase {
  CloudWatchMetricsHealthSourceSpec cloudWatchMetricsHealthSourceSpec;
  @Inject MetricPackService metricPackService;
  String orgIdentifier;
  String projectIdentifier;
  String accountId;
  String region;
  String feature;
  String connectorIdentifier;
  String serviceIdentifier;
  String envIdentifier;
  String identifier;
  String name;
  String monitoredServiceIdentifier;
  List<TimeSeriesMetricPackDTO> metricPackDTOS;
  BuilderFactory builderFactory;

  @Before
  public void setup() {
    builderFactory = BuilderFactory.getDefault();
    accountId = builderFactory.getContext().getAccountId();
    orgIdentifier = builderFactory.getContext().getOrgIdentifier();
    projectIdentifier = builderFactory.getContext().getProjectIdentifier();
    serviceIdentifier = builderFactory.getContext().getServiceIdentifier();
    envIdentifier = builderFactory.getContext().getEnvIdentifier();
    region = "us-east1";
    feature = "CloudWatch Metrics";
    connectorIdentifier = "connectorRef";
    monitoredServiceIdentifier = generateUuid();
    identifier = "identifier";
    name = "some-name";
    metricPackDTOS = Collections.singletonList(
        TimeSeriesMetricPackDTO.builder().identifier(CVNextGenConstants.PERFORMANCE_PACK_IDENTIFIER).build());
    cloudWatchMetricsHealthSourceSpec = CloudWatchMetricsHealthSourceSpec.builder()
                                            .region(region)
                                            .connectorRef(connectorIdentifier)
                                            .feature(feature)
                                            .metricPacks(new HashSet<>(metricPackDTOS))
                                            .build();

    metricPackService.createDefaultMetricPackAndThresholds(accountId, orgIdentifier, projectIdentifier);
  }

  @Test
  @Owner(developers = DHRUVX)
  @Category(UnitTests.class)
  public void testGetCVConfigUpdateResult_whenNoConfigExist() {
    CVConfigUpdateResult cvConfigUpdateResult = cloudWatchMetricsHealthSourceSpec.getCVConfigUpdateResult(accountId,
        orgIdentifier, projectIdentifier, envIdentifier, serviceIdentifier, monitoredServiceIdentifier, identifier,
        name, Collections.emptyList(), metricPackService);
    assertThat(cvConfigUpdateResult.getUpdated()).isEmpty();
    assertThat(cvConfigUpdateResult.getDeleted()).isEmpty();
    List<CVConfig> added = cvConfigUpdateResult.getAdded();

    List<CloudWatchMetricCVConfig> cvConfigs = (List<CloudWatchMetricCVConfig>) (List<?>) added;
    assertThat(cvConfigs).hasSize(1);
    CloudWatchMetricCVConfig cloudWatchMetricCVConfig = cvConfigs.get(0);
    assertCommon(cloudWatchMetricCVConfig);
    assertThat(cloudWatchMetricCVConfig.getMetricPack().getCategory()).isEqualTo(CVMonitoringCategory.PERFORMANCE);
    assertThat(cloudWatchMetricCVConfig.getMetricPack().getMetrics().size()).isEqualTo(3);
  }

  @Test
  @Owner(developers = DHRUVX)
  @Category(UnitTests.class)
  public void testGetCVConfigUpdateResult_checkDeleted() {
    List<CVConfig> cvConfigs = new ArrayList<>();
    cvConfigs.add(
        createCVConfig(MetricPack.builder().accountId(accountId).category(CVMonitoringCategory.ERRORS).build()));
    CVConfigUpdateResult result = cloudWatchMetricsHealthSourceSpec.getCVConfigUpdateResult(accountId, orgIdentifier,
        projectIdentifier, envIdentifier, serviceIdentifier, monitoredServiceIdentifier, identifier, name, cvConfigs,
        metricPackService);
    assertThat(result.getDeleted()).hasSize(1);
    CloudWatchMetricCVConfig cloudWatchMetricCVConfig = (CloudWatchMetricCVConfig) result.getDeleted().get(0);
    assertThat(cloudWatchMetricCVConfig.getMetricPack().getCategory()).isEqualTo(CVMonitoringCategory.ERRORS);
  }

  @Test
  @Owner(developers = DHRUVX)
  @Category(UnitTests.class)
  public void testGetCVConfigUpdateResult_checkAdded() {
    List<CVConfig> cvConfigs = new ArrayList<>();
    cvConfigs.add(
        createCVConfig(MetricPack.builder().accountId(accountId).category(CVMonitoringCategory.ERRORS).build()));
    CVConfigUpdateResult result = cloudWatchMetricsHealthSourceSpec.getCVConfigUpdateResult(accountId, orgIdentifier,
        projectIdentifier, envIdentifier, serviceIdentifier, monitoredServiceIdentifier, identifier, name, cvConfigs,
        metricPackService);
    assertThat(result.getAdded()).hasSize(1);
    CloudWatchMetricCVConfig cloudWatchMetricCVConfig = (CloudWatchMetricCVConfig) result.getAdded().get(0);
    assertCommon(cloudWatchMetricCVConfig);
    assertThat(cloudWatchMetricCVConfig.getMetricPack().getCategory()).isEqualTo(CVMonitoringCategory.PERFORMANCE);
  }

  @Test
  @Owner(developers = DHRUVX)
  @Category(UnitTests.class)
  public void testGetCVConfigUpdateResult_checkUpdated() {
    List<CVConfig> cvConfigs = new ArrayList<>();
    cvConfigs.add(createCVConfig(metricPackService.getMetricPack(accountId, orgIdentifier, projectIdentifier,
        DataSourceType.CLOUDWATCH_METRICS, CVNextGenConstants.PERFORMANCE_PACK_IDENTIFIER)));
    CVConfigUpdateResult result = cloudWatchMetricsHealthSourceSpec.getCVConfigUpdateResult(accountId, orgIdentifier,
        projectIdentifier, envIdentifier, serviceIdentifier, monitoredServiceIdentifier, identifier, name, cvConfigs,
        metricPackService);
    assertThat(result.getUpdated()).hasSize(1);
    CloudWatchMetricCVConfig cloudWatchMetricCVConfig = (CloudWatchMetricCVConfig) result.getUpdated().get(0);
    assertCommon(cloudWatchMetricCVConfig);
    assertThat(cloudWatchMetricCVConfig.getMetricPack().getCategory()).isEqualTo(CVMonitoringCategory.PERFORMANCE);
  }

  private void assertCommon(CloudWatchMetricCVConfig cvConfig) {
    assertThat(cvConfig.getAccountId()).isEqualTo(accountId);
    assertThat(cvConfig.getOrgIdentifier()).isEqualTo(orgIdentifier);
    assertThat(cvConfig.getProjectIdentifier()).isEqualTo(projectIdentifier);
    assertThat(cvConfig.getRegion()).isEqualTo(region);
    assertThat(cvConfig.getConnectorIdentifier()).isEqualTo(connectorIdentifier);
    assertThat(cvConfig.getIdentifier()).isEqualTo(identifier);
    assertThat(cvConfig.getProductName()).isEqualTo(feature);
    assertThat(cvConfig.getMonitoringSourceName()).isEqualTo(name);
    assertThat(cvConfig.getMetricPack().getAccountId()).isEqualTo(accountId);
    assertThat(cvConfig.getMetricPack().getOrgIdentifier()).isEqualTo(orgIdentifier);
    assertThat(cvConfig.getMetricPack().getDataSourceType()).isEqualTo(DataSourceType.CLOUDWATCH_METRICS);
    assertThat(cvConfig.getMonitoredServiceIdentifier()).isEqualTo(monitoredServiceIdentifier);
  }

  private CVConfig createCVConfig(MetricPack metricPack) {
    return builderFactory.cloudWatchMetricCVConfigBuilder()
        .region(region)
        .metricPack(metricPack)
        .connectorIdentifier(connectorIdentifier)
        .monitoringSourceName(name)
        .productName(feature)
        .monitoredServiceIdentifier(monitoredServiceIdentifier)
        .identifier(identifier)
        .build();
  }
}
