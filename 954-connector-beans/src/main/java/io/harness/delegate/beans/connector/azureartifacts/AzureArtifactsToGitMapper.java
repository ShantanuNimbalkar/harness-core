/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.beans.connector.azureartifacts;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.delegate.beans.connector.scm.GitConnectionType;
import io.harness.delegate.beans.connector.scm.adapter.GitConfigCreater;
import io.harness.delegate.beans.connector.scm.genericgitconnector.GitConfigDTO;
import io.harness.encryption.SecretRefData;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.UnknownEnumTypeException;

import lombok.experimental.UtilityClass;

@UtilityClass
@OwnedBy(HarnessTeam.CDC)
public class AzureArtifactsToGitMapper {
  public static final String GIT = "/_git/";
  public static GitConfigDTO mapToGitConfigDTO(AzureArtifactsConnectorDTO connectorDTO) {
    final GitAuthType authType = connectorDTO.getAuthentication().getAuthType();
    final GitConnectionType connectionType = mapToGitConnectionType(connectorDTO.getConnectionType());
    final String url = connectorDTO.getUrl();
    final String validationRepo = connectorDTO.getValidationRepo();
    if (authType == null) {
      throw new InvalidRequestException("Azure Artifacts DTO Auth Type not found");
    }
    switch (authType) {
      case HTTP:
        return mapToGitHTTP(connectorDTO, connectionType, url, validationRepo);
      case SSH:
        return mapToGitSSH(connectorDTO, connectionType, url, validationRepo);
      default:
        throw new InvalidRequestException("Unknown auth type: " + authType);
    }
  }

  public GitConfigDTO mapToGitHTTP(
      AzureArtifactsConnectorDTO connectorDTO, GitConnectionType connectionType, String url, String validationRepo) {
    final AzureArtifactsHttpCredentialsDTO credentials =
        (AzureArtifactsHttpCredentialsDTO) connectorDTO.getAuthentication().getCredentials();
    String username;
    SecretRefData usernameRef, tokenRef;
    final AzureArtifactsUsernameTokenDTO azureArtifactsUsernameTokenDTO =
        (AzureArtifactsUsernameTokenDTO) credentials.getHttpCredentialsSpec();
    username = azureArtifactsUsernameTokenDTO.getUsername();
    usernameRef = azureArtifactsUsernameTokenDTO.getUsernameRef();
    tokenRef = azureArtifactsUsernameTokenDTO.getTokenRef();
    validationRepo = GIT + validationRepo;
    GitConfigDTO gitConfigForHttp = GitConfigCreater.getGitConfigForHttp(
        connectionType, url, validationRepo, username, usernameRef, tokenRef, connectorDTO.getDelegateSelectors());
    gitConfigForHttp.setExecuteOnDelegate(connectorDTO.getExecuteOnDelegate());
    return gitConfigForHttp;
  }

  public GitConfigDTO mapToGitSSH(
      AzureArtifactsConnectorDTO connectorDTO, GitConnectionType connectionType, String url, String validationRepo) {
    final AzureArtifactsSshCredentialsDTO credentials =
        (AzureArtifactsSshCredentialsDTO) connectorDTO.getAuthentication().getCredentials();
    final SecretRefData sshKeyRef = credentials.getSshKeyRef();
    GitConfigDTO gitConfigForSsh = GitConfigCreater.getGitConfigForSsh(
        connectionType, url, validationRepo, sshKeyRef, connectorDTO.getDelegateSelectors());
    gitConfigForSsh.setExecuteOnDelegate(connectorDTO.getExecuteOnDelegate());
    return gitConfigForSsh;
  }

  public static GitConnectionType mapToGitConnectionType(AzureArtifactsConnectionTypeDTO connectionType) {
    switch (connectionType) {
      case PROJECT:
        return GitConnectionType.PROJECT;
      case REPO:
        return GitConnectionType.REPO;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Connection Type ", connectionType.name());
    }
  }
}
