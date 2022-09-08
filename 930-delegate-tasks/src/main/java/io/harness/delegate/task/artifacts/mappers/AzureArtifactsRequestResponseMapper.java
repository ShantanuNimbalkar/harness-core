/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.artifacts.mappers;

import io.harness.artifacts.azureartifacts.beans.AzureArtifactsInternalConfig;
import io.harness.delegate.task.artifacts.azureartifacts.AzureArtifactsDelegateRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AzureArtifactsRequestResponseMapper {
  public AzureArtifactsInternalConfig toAzureArtifactsInternalConfig(AzureArtifactsDelegateRequest request) {
    String password = "";

    String username = "";

    String token = "";

    if (request.getAzureArtifactsConnectorDTO().getAuthentication() != null
        && request.getAzureArtifactsConnectorDTO().getAuthentication().getCredentials() != null) {
    }

    return null;
  }
}
