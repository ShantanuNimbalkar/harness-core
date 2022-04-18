/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.serializer.kryo;

import io.harness.delegate.task.executioncapability.BatchCapabilityCheckTaskParameters;
import io.harness.delegate.task.executioncapability.BatchCapabilityCheckTaskResponse;
import io.harness.delegate.task.winrm.AuthenticationScheme;
import io.harness.serializer.KryoRegistrar;

import software.wings.beans.*;
import software.wings.beans.appmanifest.HelmChart;
import software.wings.beans.artifact.ArtifactFile;
import software.wings.beans.artifact.ArtifactStreamAttributes;
import software.wings.beans.artifact.ArtifactoryCollectionTaskParameters;
import software.wings.beans.command.ExecutionLogCallback;
import software.wings.beans.config.ArtifactoryConfig;
import software.wings.beans.config.LogzConfig;
import software.wings.beans.config.NexusConfig;
import software.wings.beans.settings.azureartifacts.AzureArtifactsPATConfig;
import software.wings.beans.trigger.WebHookTriggerResponseData;
import software.wings.beans.trigger.WebhookTriggerParameters;
import software.wings.delegatetasks.DelegateStateType;
import software.wings.delegatetasks.buildsource.BuildCollectParameters;
import software.wings.delegatetasks.collect.artifacts.AzureArtifactsCollectionTaskParameters;
import software.wings.delegatetasks.cv.DataCollectionException;
import software.wings.delegatetasks.cv.beans.CustomLogResponseMapper;
import software.wings.helpers.ext.azure.devops.AzureArtifactsFeed;
import software.wings.helpers.ext.azure.devops.AzureArtifactsPackageVersion;
import software.wings.helpers.ext.mail.SmtpConfig;
import software.wings.service.impl.analysis.AnalysisComparisonStrategy;
import software.wings.service.impl.analysis.CustomLogDataCollectionInfo;
import software.wings.service.impl.analysis.DataCollectionTaskResult;
import software.wings.service.impl.analysis.LogElement;
import software.wings.service.impl.analysis.SetupTestNodeData;
import software.wings.service.impl.analysis.TimeSeriesMlAnalysisType;
import software.wings.service.impl.appdynamics.AppdynamicsDataCollectionInfo;
import software.wings.service.impl.appdynamics.AppdynamicsSetupTestNodeData;
import software.wings.service.impl.aws.model.AwsRequest;
import software.wings.service.impl.aws.model.AwsResponse;
import software.wings.service.impl.aws.model.AwsS3ListBucketNamesRequest;
import software.wings.service.impl.aws.model.AwsS3ListBucketNamesResponse;
import software.wings.service.impl.aws.model.AwsS3Request;
import software.wings.service.impl.dynatrace.DynaTraceApplication;
import software.wings.service.impl.dynatrace.DynaTraceDataCollectionInfo;
import software.wings.service.impl.dynatrace.DynaTraceMetricDataResponse;
import software.wings.service.impl.dynatrace.DynaTraceSetupTestNodeData;
import software.wings.service.impl.dynatrace.DynaTraceTimeSeries;
import software.wings.service.impl.elk.ElkDataCollectionInfo;
import software.wings.service.impl.elk.ElkLogFetchRequest;
import software.wings.service.impl.elk.ElkQueryType;
import software.wings.service.impl.logz.LogzDataCollectionInfo;
import software.wings.service.impl.newrelic.NewRelicDataCollectionInfo;
import software.wings.service.impl.newrelic.NewRelicMetricDataRecord;
import software.wings.service.impl.newrelic.NewRelicSetupTestNodeData;
import software.wings.service.impl.sumo.SumoDataCollectionInfo;
import software.wings.service.intfc.analysis.ClusterLevel;
import software.wings.utils.ArtifactType;

import com.esotericsoftware.kryo.Kryo;

public class DelegateTasksKryoRegistrar implements KryoRegistrar {
  @Override
  public void register(Kryo kryo) {
    kryo.register(ArtifactStreamAttributes.class, 5007);
    kryo.register(BambooConfig.class, 5009);
    kryo.register(DockerConfig.class, 5010);
    kryo.register(EcrConfig.class, 5011);
    kryo.register(GcpConfig.class, 5014);
    kryo.register(NexusConfig.class, 5016);
    kryo.register(ElkConfig.class, 5017);
    kryo.register(ArtifactoryConfig.class, 5018);
    kryo.register(ExecutionLogCallback.class, 5044);
    kryo.register(ArtifactFile.class, 5066);
    kryo.register(HostConnectionAttributes.class, 5070);
    kryo.register(HostConnectionAttributes.ConnectionType.class, 5071);
    kryo.register(ElkDataCollectionInfo.class, 5169);
    kryo.register(LogzDataCollectionInfo.class, 5170);
    kryo.register(NewRelicDataCollectionInfo.class, 5171);
    kryo.register(SumoDataCollectionInfo.class, 5173);
    kryo.register(NewRelicConfig.class, 5175);
    kryo.register(LogzConfig.class, 5176);
    kryo.register(SplunkConfig.class, 5177);
    kryo.register(SumoConfig.class, 5178);
    kryo.register(DynaTraceTimeSeries.class, 5239);
    kryo.register(DynaTraceConfig.class, 5237);
    kryo.register(DynaTraceDataCollectionInfo.class, 5238);
    kryo.register(AnalysisComparisonStrategy.class, 5240);
    kryo.register(AzureConfig.class, 5242);
    kryo.register(ElkQueryType.class, 5275);
    kryo.register(PcfConfig.class, 5296);
    kryo.register(SmtpConfig.class, 5304);
    kryo.register(ElkLogFetchRequest.class, 5376);
    kryo.register(AwsRequest.class, 5380);
    kryo.register(AwsResponse.class, 5381);
    kryo.register(DynaTraceMetricDataResponse.class, 5513);
    kryo.register(DynaTraceMetricDataResponse.DynaTraceMetricDataResult.class, 5514);
    kryo.register(DynaTraceSetupTestNodeData.class, 5512);
    kryo.register(NewRelicSetupTestNodeData.class, 5529);
    kryo.register(SmbConfig.class, 5551);
    kryo.register(SftpConfig.class, 5560);
    kryo.register(JiraConfig.JiraSetupType.class, 5569);
    kryo.register(JiraConfig.class, 5581);
    kryo.register(ServiceNowConfig.class, 7155);
    kryo.register(InstanaConfig.class, 7293);
    kryo.register(DataCollectionException.class, 7298);
    kryo.register(BatchCapabilityCheckTaskParameters.class, 8200);
    kryo.register(BatchCapabilityCheckTaskResponse.class, 8201);
    kryo.register(WebhookTriggerParameters.class, 8550);
    kryo.register(WebHookTriggerResponseData.class, 8552);
    kryo.register(AuthenticationScheme.class, 8600);
    kryo.register(AppDynamicsConfig.class, 5074);
    kryo.register(CustomLogDataCollectionInfo.class, 5492);
    kryo.register(DataCollectionTaskResult.DataCollectionTaskStatus.class, 5185);
    kryo.register(DataCollectionTaskResult.class, 5184);
    kryo.register(LogElement.class, 5486);
    kryo.register(SetupTestNodeData.class, 5530);
    kryo.register(AwsS3Request.class, 7266);
    kryo.register(AwsS3Request.AwsS3RequestType.class, 7267);
    kryo.register(AwsS3ListBucketNamesRequest.class, 7268);
    kryo.register(AwsS3ListBucketNamesResponse.class, 7269);
    kryo.register(AzureArtifactsPATConfig.class, 7284);
    kryo.register(AzureArtifactsFeed.class, 7286);
    kryo.register(AzureArtifactsPackageVersion.class, 7288);
    kryo.register(AzureArtifactsCollectionTaskParameters.class, 7289);
    kryo.register(ClusterLevel.class, 7348);
    kryo.register(SetupTestNodeData.Instance.class, 7470);
    kryo.register(AppdynamicsSetupTestNodeData.class, 5531);
    kryo.register(AppdynamicsDataCollectionInfo.class, 5168);
    kryo.register(TimeSeriesMlAnalysisType.class, 5347);
    kryo.register(CustomLogResponseMapper.class, 5493);
    kryo.register(NewRelicMetricDataRecord.class, 7347);
    kryo.register(DynaTraceApplication.class, 8074);
    kryo.register(ArtifactType.class, 5117);
    kryo.register(ArtifactoryCollectionTaskParameters.class, 8203);
    kryo.register(DelegateStateType.class, 8601);
    kryo.register(BuildCollectParameters.class, 8602);
    kryo.register(SSHVaultConfig.class, 15012);
    kryo.register(BaseVaultConfig.class, 15014);
    kryo.register(HelmChart.class, 71106);
  }
}
