/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.beans.connector.azureartifacts;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.DecryptableEntity;
import io.harness.delegate.beans.connector.scm.GitConfigConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.v3.oas.annotations.media.Schema;

@OwnedBy(HarnessTeam.CDC)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AzureArtifactsHttpCredentialsDTO.class, name = GitConfigConstants.HTTP)
  , @JsonSubTypes.Type(value = AzureArtifactsSshCredentialsDTO.class, name = GitConfigConstants.SSH)
})
@Schema(
    name = "AzureArtifactsCredentials", description = "This is a interface for details of the Azure DevOps credentials")
public interface AzureArtifactsCredentialsDTO extends DecryptableEntity {}
