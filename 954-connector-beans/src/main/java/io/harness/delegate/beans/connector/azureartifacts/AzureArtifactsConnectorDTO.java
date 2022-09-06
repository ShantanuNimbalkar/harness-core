/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.beans.connector.azureartifacts;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.utils.FilePathUtils.removeStartingAndEndingSlash;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.DecryptableEntity;
import io.harness.connector.DelegateSelectable;
import io.harness.connector.ManagerExecutable;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.delegate.beans.connector.scm.ScmConnector;
import io.harness.delegate.beans.connector.scm.utils.ScmConnectorHelper;
import io.harness.exception.InvalidRequestException;
import io.harness.git.GitClientHelper;
import io.harness.gitsync.beans.GitRepositoryDTO;
import io.harness.utils.FilePathUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ApiModel("AzureArtifactsConnector")
@OwnedBy(HarnessTeam.CDC)
@Schema(name = "AzureArtifactsConfig", description = "This contains details of AzureArtifacts connector")
public class AzureArtifactsConnectorDTO
    extends ConnectorConfigDTO implements ScmConnector, DelegateSelectable, ManagerExecutable {
  @NotNull
  @JsonProperty("type")
  @Schema(description = "Project or Repository connector type")
  AzureArtifactsConnectionTypeDTO connectionType;

  @NotBlank @NotNull @Schema(description = "SSH or HTTP URL based on type of connection") String url;

  @Schema(description = "The repo to validate AzureArtifacts credentials. Only valid for Account type connector")
  String validationRepo;

  @Valid
  @NotNull
  @Schema(description = "Details for authentication mechanism for AzureArtifacts connector")
  AzureArtifactsAuthenticationDTO authentication;

  @Valid
  @Schema(description = "API access details, to be used in Harness Triggers and Git Experience")
  AzureArtifactsApiAccessDTO apiAccess;

  @Schema(description = "Selected Connectivity Modes") Set<String> delegateSelectors;

  Boolean executeOnDelegate = true;

  @Schema(description = "Connection URL for connecting AzureArtifacts") String gitConnectionUrl;

  private static final String AZURE_ARTIFACTS_NAME_SEPARATOR = "_git/";

  @Builder
  public AzureArtifactsConnectorDTO(AzureArtifactsConnectionTypeDTO connectionType, String url, String validationRepo,
      AzureArtifactsAuthenticationDTO authentication, AzureArtifactsApiAccessDTO apiAccess,
      Set<String> delegateSelectors, Boolean executeOnDelegate) {
    this.connectionType = connectionType;
    this.url = url;
    this.validationRepo = validationRepo;
    this.authentication = authentication;
    this.apiAccess = apiAccess;
    this.delegateSelectors = delegateSelectors;
    this.executeOnDelegate = executeOnDelegate;
  }

  @Override
  public List<DecryptableEntity> getDecryptableEntities() {
    List<DecryptableEntity> decryptableEntities = new ArrayList<>();
    if (authentication.getAuthType() == GitAuthType.HTTP) {
      AzureArtifactsHttpCredentialsSpecDTO httpCredentialsSpec =
          ((AzureArtifactsHttpCredentialsSpecDTO) authentication.getCredentials()).getHttpCredentialsSpec();
      if (httpCredentialsSpec != null) {
        decryptableEntities.add(httpCredentialsSpec);
      }
    } else {
      AzureArtifactsSshCredentialsDTO sshCredential = (AzureArtifactsSshCredentialsDTO) authentication.getCredentials();
      if (sshCredential != null) {
        decryptableEntities.add(sshCredential);
      }
    }
    if (apiAccess != null && apiAccess.getSpec() != null) {
      decryptableEntities.add(apiAccess.getSpec());
    }
    return decryptableEntities;
  }

  @Override
  public String getUrl() {
    if (isNotEmpty(gitConnectionUrl)) {
      return gitConnectionUrl;
    }
    return url;
  }

  @Override
  @JsonIgnore
  public ConnectorType getConnectorType() {
    return ConnectorType.AZURE_ARTIFACTS;
  }

  @Override
  public String getGitConnectionUrl(GitRepositoryDTO gitRepositoryDTO) {
    if (connectionType == AzureArtifactsConnectionTypeDTO.REPO) {
      String linkedRepo = getGitRepositoryDetails().getName();
      if (!linkedRepo.equals(gitRepositoryDTO.getName())) {
        throw new InvalidRequestException(
            String.format("Provided repoName [%s] does not match with the repoName [%s] provided in connector.",
                gitRepositoryDTO.getName(), linkedRepo));
      }
      return url;
    }
    if (GitAuthType.SSH.equals(authentication.getAuthType())) {
      return FilePathUtils.addEndingSlashIfMissing(url) + gitRepositoryDTO.getName();
    }
    return FilePathUtils.addEndingSlashIfMissing(url) + AZURE_ARTIFACTS_NAME_SEPARATOR + gitRepositoryDTO.getName();
  }

  @Override
  public GitRepositoryDTO getGitRepositoryDetails() {
    String orgAndProject;
    if (GitAuthType.HTTP.equals(authentication.getAuthType())) {
      orgAndProject = GitClientHelper.getAzureRepoOrgAndProjectHTTP(url);
    } else {
      orgAndProject = GitClientHelper.getAzureRepoOrgAndProjectSSH(url);
    }
    GitRepositoryDTO.GitRepositoryDTOBuilder gitRepositoryDTOBuilder =
        GitRepositoryDTO.builder()
            .org(GitClientHelper.getAzureRepoOrg(orgAndProject))
            .projectName(GitClientHelper.getAzureRepoProject(orgAndProject));

    if (AzureArtifactsConnectionTypeDTO.REPO.equals(connectionType)) {
      String repoName = GitClientHelper.getGitRepo(url);
      repoName = StringUtils.substringAfterLast(repoName, "/");
      return gitRepositoryDTOBuilder.name(repoName).build();
    }
    return gitRepositoryDTOBuilder.build();
  }

  @Override
  public String getFileUrl(String branchName, String filePath, GitRepositoryDTO gitRepositoryDTO) {
    final String FILE_URL_FORMAT = "%s?path=%s&version=GB%s";
    ScmConnectorHelper.validateGetFileUrlParams(branchName, filePath);
    String repoUrl = removeStartingAndEndingSlash(getGitConnectionUrl(gitRepositoryDTO));
    String httpRepoUrl = GitClientHelper.getCompleteHTTPRepoUrlForAzureRepoSaas(repoUrl);
    filePath = removeStartingAndEndingSlash(filePath);
    return String.format(FILE_URL_FORMAT, httpRepoUrl, filePath, branchName);
  }

  @Override
  public void validate() {
    GitClientHelper.validateURL(url);
  }
}
