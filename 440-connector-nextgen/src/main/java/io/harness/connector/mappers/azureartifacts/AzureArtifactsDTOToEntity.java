/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.connector.mappers.azureartifacts;

import io.harness.connector.entities.embedded.azureartifacts.*;
import io.harness.connector.mappers.ConnectorDTOToEntityMapper;
import io.harness.delegate.beans.connector.azureartifacts.*;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.encryption.SecretRefData;
import io.harness.encryption.SecretRefHelper;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.UnknownEnumTypeException;

public class AzureArtifactsDTOToEntity
    implements ConnectorDTOToEntityMapper<AzureArtifactsConnectorDTO, AzureArtifactsConnector> {
  @Override
  public AzureArtifactsConnector toConnectorEntity(AzureArtifactsConnectorDTO configDTO) {
    if (configDTO == null) {
      throw new InvalidRequestException("AzureArtifacts Config DTO is not found");
    }
    if (configDTO.getAuthentication() == null) {
      throw new InvalidRequestException("No Authentication Details Found in the connector");
    }
    GitAuthType gitAuthType = getAuthType(configDTO.getAuthentication());
    AzureArtifactsAuthentication azureArtifactsAuthentication =
        buildAuthenticationDetails(gitAuthType, configDTO.getAuthentication().getCredentials());
    boolean hasApiAccess = hasApiAccess(configDTO.getApiAccess());
    AzureArtifactsApiAccessType apiAccessType = null;
    AzureArtifactsApiAccessDetails azureArtifactsApiAccess = null;
    if (hasApiAccess) {
      apiAccessType = getApiAccessType(configDTO.getApiAccess());
      azureArtifactsApiAccess = getApiAcessByType(configDTO.getApiAccess().getSpec(), apiAccessType);
    }
    return AzureArtifactsConnector.builder()
        .connectionType(getAzureArtifactsConnectionType(configDTO.getConnectionType()))
        .authType(gitAuthType)
        .hasApiAccess(hasApiAccess)
        .apiAccessType(apiAccessType)
        .authenticationDetails(azureArtifactsAuthentication)
        .azureArtifactsApiAccess(azureArtifactsApiAccess)
        .url(configDTO.getUrl())
        .validationRepo(configDTO.getValidationRepo())
        .build();
  }

  public AzureArtifactsAuthentication buildAuthenticationDetails(
      GitAuthType gitAuthType, AzureArtifactsCredentialsDTO credentialsDTO) {
    if (gitAuthType == null) {
      throw new InvalidRequestException("Auth Type not found");
    }
    switch (gitAuthType) {
      case SSH:
        final AzureArtifactsSshCredentialsDTO sshCredentialsDTO = (AzureArtifactsSshCredentialsDTO) credentialsDTO;
        return AzureArtifactsSshAuthentication.builder()
            .sshKeyRef(SecretRefHelper.getSecretConfigString(sshCredentialsDTO.getSshKeyRef()))
            .build();
      case HTTP:
        final AzureArtifactsHttpCredentialsDTO httpCredentialsDTO = (AzureArtifactsHttpCredentialsDTO) credentialsDTO;
        final AzureArtifactsHttpAuthenticationType type = httpCredentialsDTO.getType();
        return AzureArtifactsHttpAuthentication.builder()
            .type(type)
            .auth(getHttpAuth(type, httpCredentialsDTO))
            .build();
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Auth Type ", gitAuthType.getDisplayName());
    }
  }

  private AzureArtifactsHttpAuth getHttpAuth(
      AzureArtifactsHttpAuthenticationType type, AzureArtifactsHttpCredentialsDTO httpCredentialsDTO) {
    if (type == null) {
      throw new InvalidRequestException("AzureArtifacts Http Auth Type not found");
    }
    switch (type) {
      case USERNAME_AND_TOKEN:
        final AzureArtifactsUsernameTokenDTO azureArtifactsUsernameTokenDTO =
            (AzureArtifactsUsernameTokenDTO) httpCredentialsDTO.getHttpCredentialsSpec();
        String usernameReference = getUsernameRefFromSecret(azureArtifactsUsernameTokenDTO.getUsernameRef());
        return AzureArtifactsUsernameToken.builder()
            .tokenRef(SecretRefHelper.getSecretConfigString(azureArtifactsUsernameTokenDTO.getTokenRef()))
            .username(azureArtifactsUsernameTokenDTO.getUsername())
            .usernameRef(usernameReference)
            .build();
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Http Auth Type ", type.getDisplayName());
    }
  }

  private String getUsernameRefFromSecret(SecretRefData secretRefData) {
    String usernameRef = null;
    if (secretRefData != null) {
      usernameRef = SecretRefHelper.getSecretConfigString(secretRefData);
    }
    return usernameRef;
  }

  private AzureArtifactsApiAccess getApiAcessByType(
      AzureArtifactsApiAccessSpecDTO spec, AzureArtifactsApiAccessType apiAccessType) {
    if (apiAccessType == null) {
      throw new InvalidRequestException("AzureArtifacts Api Access Type not found");
    }
    switch (apiAccessType) {
      case TOKEN:
        final AzureArtifactsTokenSpecDTO tokenSpec = (AzureArtifactsTokenSpecDTO) spec;
        return AzureArtifactsTokenApiAccess.builder()
            .tokenRef(SecretRefHelper.getSecretConfigString(tokenSpec.getTokenRef()))
            .build();
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Api Access Type ", apiAccessType.getDisplayName());
    }
  }

  private AzureArtifactsApiAccessType getApiAccessType(AzureArtifactsApiAccessDTO apiAccess) {
    return apiAccess.getType();
  }

  private boolean hasApiAccess(AzureArtifactsApiAccessDTO apiAccess) {
    return apiAccess != null;
  }

  private GitAuthType getAuthType(AzureArtifactsAuthenticationDTO authentication) {
    return authentication.getAuthType();
  }

  @VisibleForTesting
  public AzureArtifactsConnectionType getAzureArtifactsConnectionType(AzureArtifactsConnectionTypeDTO connectionType) {
    switch (connectionType) {
      case PROJECT:
        return AzureArtifactsConnectionType.PROJECT;
      case REPO:
        return AzureArtifactsConnectionType.REPO;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Connection Type ", connectionType.name());
    }
  }
}
