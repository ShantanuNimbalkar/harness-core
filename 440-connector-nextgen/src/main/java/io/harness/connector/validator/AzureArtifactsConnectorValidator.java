/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.connector.validator;

import static software.wings.beans.TaskType.AZURE_ARTIFACTS_CONNECTIVITY_TEST_TASK;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.ConnectorResponseDTO;
import io.harness.connector.ConnectorValidationResult;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.azureartifacts.AzureArtifactsConnectorDTO;
import io.harness.delegate.beans.connector.azureartifacts.AzureArtifactsCredentialsDTO;
import io.harness.delegate.beans.connector.azureartifacts.AzureArtifactsTestConnectionTaskParams;
import io.harness.delegate.beans.connector.azureartifacts.AzureArtifactsTestConnectionTaskResponse;
import io.harness.delegate.task.TaskParameters;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CDC)
@Slf4j
@Singleton
public class AzureArtifactsConnectorValidator extends AbstractConnectorValidator {
  @Override
  public <T extends ConnectorConfigDTO> TaskParameters getTaskParameters(
      T connectorConfig, String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    AzureArtifactsConnectorDTO azureArtifactsConnectorDTO = (AzureArtifactsConnectorDTO) connectorConfig;
    AzureArtifactsCredentialsDTO azureArtifactsCredentialsDTO = azureArtifactsConnectorDTO.getAuthentication() != null
        ? azureArtifactsConnectorDTO.getAuthentication().getCredentials()
        : null;
    return AzureArtifactsTestConnectionTaskParams.builder()
        .azureArtifactsConnector(azureArtifactsConnectorDTO)
        .encryptionDetails(super.getEncryptionDetail(
            azureArtifactsCredentialsDTO, accountIdentifier, orgIdentifier, projectIdentifier))
        .build();
  }

  @Override
  public String getTaskType() {
    return AZURE_ARTIFACTS_CONNECTIVITY_TEST_TASK.name();
  }

  @Override
  public ConnectorValidationResult validate(ConnectorConfigDTO azureArtifactsConnector, String accountIdentifier,
      String orgIdentifier, String projectIdentifier, String identifier) {
    AzureArtifactsTestConnectionTaskResponse responseData =
        (AzureArtifactsTestConnectionTaskResponse) super.validateConnector(
            azureArtifactsConnector, accountIdentifier, orgIdentifier, projectIdentifier, identifier);
    return responseData.getConnectorValidationResult();
  }

  @Override
  public ConnectorValidationResult validate(ConnectorResponseDTO connectorResponseDTO, String accountIdentifier,
      String orgIdentifier, String projectIdentifier, String identifier) {
    return null;
  }
}
