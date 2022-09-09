/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.artifacts.azureartifacts;

import io.harness.artifacts.azureartifacts.service.AzureArtifactsRegistryService;
import io.harness.delegate.task.artifacts.DelegateArtifactTaskHandler;
import io.harness.delegate.task.artifacts.jenkins.JenkinsArtifactDelegateRequest;
import io.harness.delegate.task.artifacts.mappers.AzureArtifactsRequestResponseMapper;
import io.harness.delegate.task.artifacts.response.ArtifactTaskExecutionResponse;
import io.harness.security.encryption.SecretDecryptionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.concurrent.ExecutorService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({ @Inject }))
@Slf4j
public class AzureArtifactsTaskHandler extends DelegateArtifactTaskHandler<AzureArtifactsDelegateRequest> {
  private static final int ARTIFACT_RETENTION_SIZE = 25;
  private static final int MAX_RETRY = 5;
  private final SecretDecryptionService secretDecryptionService;
  private final AzureArtifactsRegistryService azureArtifactsRegistryService;
  @Inject @Named("azureArtifactsExecutor") private ExecutorService azureArtifactsExecutor;

  @Override
  public ArtifactTaskExecutionResponse validateArtifactServer(AzureArtifactsDelegateRequest attributesRequest) {
    boolean isServerValidated = azureArtifactsRegistryService.validateCredentials(
        AzureArtifactsRequestResponseMapper.toAzureArtifactsInternalConfig(attributesRequest));

    return ArtifactTaskExecutionResponse.builder().isArtifactServerValid(isServerValidated).build();
  }

  @Override
  public void decryptRequestDTOs(AzureArtifactsDelegateRequest attributesRequest) {}
}
