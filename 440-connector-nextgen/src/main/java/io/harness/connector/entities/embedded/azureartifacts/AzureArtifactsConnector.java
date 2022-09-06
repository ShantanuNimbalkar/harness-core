/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.connector.entities.embedded.azureartifacts;

import io.harness.connector.entities.Connector;
import io.harness.delegate.beans.connector.azureartifacts.AzureArtifactsApiAccessType;
import io.harness.delegate.beans.connector.scm.GitAuthType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.annotation.TypeAlias;

@Value
@Builder
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(innerTypeName = "AzureArtifactsConnectorKeys")
@Entity(value = "connectors", noClassnameStored = true)
@Persistent
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("io.harness.connector.entities.embedded.azureartifacts.AzureArtifactsConnector")
public class AzureArtifactsConnector extends Connector {
  /**
   * Connection URL
   */
  String url;

  /**
   * Connection Type - Project / Repo level
   */
  @NotEmpty AzureArtifactsConnectionType connectionType;

  /**
   * For Project Connection Type - test repo for connection
   */
  String validationRepo;

  /**
   * Git auth type - HTTP or SSH
   */
  @NotEmpty GitAuthType authType;

  /**
   * Authentication Details
   */
  @NotEmpty AzureArtifactsAuthentication authenticationDetails;

  /**
   * is API Access enabled
   */
  @NotEmpty boolean hasApiAccess;

  /**
   * API Acess Type - TOKEN
   */
  AzureArtifactsApiAccessType apiAccessType;

  /**
   * Authentication Details
   */
  AzureArtifactsApiAccessDetails azureRepoApiAccess;
}
