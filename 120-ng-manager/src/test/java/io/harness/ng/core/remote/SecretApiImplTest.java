/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.core.remote;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.exception.InvalidRequestException;
import io.harness.ng.core.api.NGEncryptedDataService;
import io.harness.ng.core.api.SecretCrudService;
import io.harness.ng.core.api.impl.SecretCrudServiceImpl;
import io.harness.ng.core.api.impl.SecretPermissionValidator;
import io.harness.ng.core.dto.secrets.SecretDTOV2;
import io.harness.ng.core.dto.secrets.SecretResponseWrapper;
import io.harness.rule.Owner;
import io.harness.spec.server.ng.model.Secret;
import io.harness.spec.server.ng.model.SecretRequest;
import io.harness.spec.server.ng.model.SecretResponse;
import io.harness.spec.server.ng.model.SecretSpec;
import io.harness.spec.server.ng.model.SecretTextSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.service.impl.security.NGEncryptorService;

import javax.validation.Validator;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.ng.core.remote.SecretApiMapper.toSecretDto;
import static io.harness.rule.OwnerRule.ASHISHSANODIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@OwnedBy(PL)
public class SecretApiImplTest extends CategoryTest {
  private SecretCrudService ngSecretService;
  private SecretPermissionValidator secretPermissionValidator;

  private AccountSecretApiImpl accountSecretApi;
  private OrgSecretApiImpl orgSecretApi;
  private ProjectSecretApiImpl projectSecretApi;

  private String account = "account";
  private String org = "org";
  private String project = "project";
  private Boolean privateSecret = false;
  private String slug = "secret_slug";
  private String name = "secret_name";
  private String secretManagerSlug = "secretManagerSlug";
  private String secretValue = "secret_value";

  @Before
  public void setup() {
    ngSecretService = mock(SecretCrudServiceImpl.class);
    secretPermissionValidator = mock(SecretPermissionValidator.class);

    doNothing().when(secretPermissionValidator).checkForAccessOrThrow(any(), any(), any(), any());

    accountSecretApi = new AccountSecretApiImpl(ngSecretService, secretPermissionValidator);
    orgSecretApi = new OrgSecretApiImpl(ngSecretService, secretPermissionValidator);
    projectSecretApi = new ProjectSecretApiImpl(ngSecretService, secretPermissionValidator);
  }

  @Test
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateAccountScopedSecret(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(null, null));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();

    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    Response response = accountSecretApi.createAccountScopedSecret(secretRequest, account, privateSecret);

    SecretResponse secretResponse = (SecretResponse)response.getEntity();
    assertThat(secretResponse.getSecret().getOrg()).isNull();
    assertThat(secretResponse.getSecret().getProject()).isNull();
    assertThat(secretResponse.getSecret().getSlug()).isEqualTo(slug);
    assertThat(secretResponse.getSecret().getName()).isEqualTo(name);
  }

  @Test(expected = InvalidRequestException.class)
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateAccountScopedSecretInvalidRequestException(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(org, project));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();
    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    accountSecretApi.createAccountScopedSecret(secretRequest, account, privateSecret);
  }

  @Test
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateOrgScopedSecret(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(org, null));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();
    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    Response response = orgSecretApi.createOrgScopedSecret(secretRequest, org, account, privateSecret);

    SecretResponse secretResponse = (SecretResponse)response.getEntity();
    assertThat(secretResponse.getSecret().getProject()).isNull();
    assertThat(secretResponse.getSecret().getOrg()).isEqualTo(org);
    assertThat(secretResponse.getSecret().getSlug()).isEqualTo(slug);
    assertThat(secretResponse.getSecret().getName()).isEqualTo(name);
  }

  @Test(expected = InvalidRequestException.class)
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateOrgScopedSecretInvalidRequestException(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(null, null));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();
    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    orgSecretApi.createOrgScopedSecret(secretRequest, org, account, privateSecret);
  }

  @Test
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateProjectScopedSecret(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(org, project));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();
    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    Response response = projectSecretApi.createProjectScopedSecret(secretRequest, org, project, account, privateSecret);

    SecretResponse secretResponse = (SecretResponse)response.getEntity();
    assertThat(secretResponse.getSecret().getProject()).isEqualTo(project);
    assertThat(secretResponse.getSecret().getOrg()).isEqualTo(org);
    assertThat(secretResponse.getSecret().getSlug()).isEqualTo(slug);
    assertThat(secretResponse.getSecret().getName()).isEqualTo(name);
  }

  @Test(expected = InvalidRequestException.class)
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testCreateProjectScopedSecretInvalidRequestException(){
    SecretRequest secretRequest = new SecretRequest();
    secretRequest.setSecret(getTextSecret(null, null));

    SecretDTOV2 secretDTOV2 = toSecretDto(secretRequest.getSecret());
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();
    when(ngSecretService.create(any(), any())).thenReturn(secretResponseWrapper);

    projectSecretApi.createProjectScopedSecret(secretRequest, org, project, account, privateSecret);
  }

  @Test
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testGetAccountScopedSecret(){
    Secret textSecret = getTextSecret(null, null);
    SecretDTOV2 secretDTOV2 = toSecretDto(textSecret);
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();

    when(ngSecretService.get(account, null, null, slug)).thenReturn(Optional.of(secretResponseWrapper));

    Response response = accountSecretApi.getAccountScopedSecret(slug, account);

    SecretResponse secretResponse = (SecretResponse)response.getEntity();
    assertThat(secretResponse.getSecret().getProject()).isNull();
    assertThat(secretResponse.getSecret().getOrg()).isNull();
    assertThat(secretResponse.getSecret().getSlug()).isEqualTo(slug);
    assertThat(secretResponse.getSecret().getName()).isEqualTo(name);
  }

  @Test(expected = NotFoundException.class)
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testGetAccountScopedSecretNotFoundException(){
    accountSecretApi.getAccountScopedSecret(slug, account);
  }

  @Test
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testGetOrgScopedSecret(){
    Secret textSecret = getTextSecret(org, null);
    SecretDTOV2 secretDTOV2 = toSecretDto(textSecret);
    SecretResponseWrapper secretResponseWrapper = SecretResponseWrapper.builder().secret(secretDTOV2).build();

    when(ngSecretService.get(account, org, null, slug)).thenReturn(Optional.of(secretResponseWrapper));

    Response response = orgSecretApi.getOrgScopedSecret(org, slug, account);

    SecretResponse secretResponse = (SecretResponse)response.getEntity();
    assertThat(secretResponse.getSecret().getProject()).isNull();
    assertThat(secretResponse.getSecret().getOrg()).isEqualTo(org);
    assertThat(secretResponse.getSecret().getSlug()).isEqualTo(slug);
    assertThat(secretResponse.getSecret().getName()).isEqualTo(name);
  }

  @Test(expected = NotFoundException.class)
  @Owner(developers = ASHISHSANODIA)
  @Category(UnitTests.class)
  public void testGetOrgScopedSecretNotFoundException(){
    orgSecretApi.getOrgScopedSecret(org, slug, account);
  }

  private Secret getTextSecret(String org, String project) {
    Secret secret = new Secret();
    secret.setSlug(slug);
    secret.setName(name);
    secret.setOrg(org);
    secret.setProject(project);

    SecretTextSpec secretTextSpec = new SecretTextSpec();
    secretTextSpec.setType(SecretSpec.TypeEnum.SECRETTEXT);
    secretTextSpec.secretManagerSlug(secretManagerSlug);
    secretTextSpec.setValue(secretValue);
    secretTextSpec.setValueType(SecretTextSpec.ValueTypeEnum.INLINE);
    secret.setSpec(secretTextSpec);
    return secret;
  }
}
