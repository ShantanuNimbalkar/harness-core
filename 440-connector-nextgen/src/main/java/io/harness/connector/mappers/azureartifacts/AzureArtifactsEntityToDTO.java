/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.connector.mappers.azureartifacts;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.entities.embedded.azureartifacts.*;
import io.harness.connector.mappers.ConnectorEntityToDTOMapper;
import io.harness.delegate.beans.connector.azureartifacts.*;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.encryption.SecretRefData;
import io.harness.encryption.SecretRefHelper;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.UnknownEnumTypeException;

@OwnedBy(HarnessTeam.CDC)
public class AzureArtifactsEntityToDTO
    implements ConnectorEntityToDTOMapper<AzureArtifactsConnectorDTO, AzureArtifactsConnector> {
  @Override
  public AzureArtifactsConnectorDTO createConnectorDTO(AzureArtifactsConnector connector) {
    if (connector == null) {
      throw new InvalidRequestException("Connector object not found");
    }
    AzureArtifactsAuthenticationDTO azureAuthenticationDTO =
        buildAzureArtifactsAuthentication(connector.getAuthType(), connector.getAuthenticationDetails());
    AzureArtifactsApiAccessDTO azureApiAccess = null;
    if (connector.isHasApiAccess()) {
      azureApiAccess = buildApiAccess(connector);
    }
    return AzureArtifactsConnectorDTO.builder()
        .apiAccess(azureApiAccess)
        .connectionType(getAzureArtifactsConnectionTypeDTO(connector.getConnectionType()))
        .authentication(azureAuthenticationDTO)
        .url(connector.getUrl())
        .validationRepo(connector.getValidationRepo())
        .build();
  }

  public AzureArtifactsAuthenticationDTO buildAzureArtifactsAuthentication(
      GitAuthType authType, AzureArtifactsAuthentication authenticationDetails) {
    AzureArtifactsCredentialsDTO azureCredentialsDTO = null;
    if (authType == null) {
      throw new InvalidRequestException("AzureArtifacts Auth Type not found");
    }
    switch (authType) {
      case SSH:
        final AzureArtifactsSshAuthentication azureSshAuthentication =
            (AzureArtifactsSshAuthentication) authenticationDetails;
        azureCredentialsDTO = AzureArtifactsSshCredentialsDTO.builder()
                                  .sshKeyRef(SecretRefHelper.createSecretRef(azureSshAuthentication.getSshKeyRef()))
                                  .build();
        break;
      case HTTP:
        final AzureArtifactsHttpAuthentication azureHttpAuthentication =
            (AzureArtifactsHttpAuthentication) authenticationDetails;
        final AzureArtifactsHttpAuthenticationType type = azureHttpAuthentication.getType();
        final AzureArtifactsHttpAuth auth = azureHttpAuthentication.getAuth();
        AzureArtifactsHttpCredentialsSpecDTO azureHttpCredentialsSpecDTO = getHttpCredentialsSpecDTO(type, auth);
        azureCredentialsDTO = AzureArtifactsHttpCredentialsDTO.builder()
                                  .type(type)
                                  .httpCredentialsSpec(azureHttpCredentialsSpecDTO)
                                  .build();
        break;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Auth Type", authType.getDisplayName());
    }
    return AzureArtifactsAuthenticationDTO.builder().authType(authType).credentials(azureCredentialsDTO).build();
  }

  private AzureArtifactsHttpCredentialsSpecDTO getHttpCredentialsSpecDTO(
      AzureArtifactsHttpAuthenticationType type, Object auth) {
    AzureArtifactsHttpCredentialsSpecDTO azureHttpCredentialsSpecDTO = null;
    if (type == null) {
      throw new InvalidRequestException("AzureArtifacts Http Auth Type not found");
    }
    switch (type) {
      case USERNAME_AND_TOKEN:
        final AzureArtifactsUsernameToken usernameToken = (AzureArtifactsUsernameToken) auth;
        SecretRefData usernameReference = null;
        if (usernameToken.getUsernameRef() != null) {
          usernameReference = SecretRefHelper.createSecretRef(usernameToken.getUsernameRef());
        }
        azureHttpCredentialsSpecDTO = AzureArtifactsUsernameTokenDTO.builder()
                                          .username(usernameToken.getUsername())
                                          .usernameRef(usernameReference)
                                          .tokenRef(SecretRefHelper.createSecretRef(usernameToken.getTokenRef()))
                                          .build();
        break;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Http Auth Type", type.getDisplayName());
    }
    return azureHttpCredentialsSpecDTO;
  }

  private AzureArtifactsApiAccessDTO buildApiAccess(AzureArtifactsConnector connector) {
    final AzureArtifactsApiAccessType apiAccessType = connector.getApiAccessType();
    AzureArtifactsApiAccessSpecDTO apiAccessSpecDTO = null;
    if (apiAccessType == null) {
      throw new InvalidRequestException("AzureArtifacts Api Access Type not found");
    }
    switch (apiAccessType) {
      case TOKEN:
        final AzureArtifactsTokenApiAccess azureTokenApiAccess =
            (AzureArtifactsTokenApiAccess) connector.getAzureArtifactsApiAccess();
        apiAccessSpecDTO = AzureArtifactsTokenSpecDTO.builder()
                               .tokenRef(SecretRefHelper.createSecretRef(azureTokenApiAccess.getTokenRef()))
                               .build();
        break;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Api Access Type", apiAccessType.getDisplayName());
    }
    return AzureArtifactsApiAccessDTO.builder().type(apiAccessType).spec(apiAccessSpecDTO).build();
  }

  @VisibleForTesting
  public AzureArtifactsConnectionTypeDTO getAzureArtifactsConnectionTypeDTO(
      AzureArtifactsConnectionType connectionType) {
    switch (connectionType) {
      case PROJECT:
        return AzureArtifactsConnectionTypeDTO.PROJECT;
      case REPO:
        return AzureArtifactsConnectionTypeDTO.REPO;
      default:
        throw new UnknownEnumTypeException("AzureArtifacts Connection Type ", connectionType.name());
    }
  }
}
