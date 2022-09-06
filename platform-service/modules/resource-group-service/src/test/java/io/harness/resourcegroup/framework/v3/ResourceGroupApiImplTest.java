/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.resourcegroup.framework.v3;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.rule.OwnerRule.MANKRIT;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.resourcegroup.beans.ScopeFilterType;
import io.harness.resourcegroup.framework.v2.service.ResourceGroupService;
import io.harness.resourcegroup.framework.v2.service.impl.ResourceGroupValidatorImpl;
import io.harness.resourcegroup.framework.v3.api.AccountResourceGroupApiImpl;
import io.harness.resourcegroup.framework.v3.api.OrgResourceGroupsApiImpl;
import io.harness.resourcegroup.framework.v3.api.ProjectResourceGroupsApiImpl;
import io.harness.resourcegroup.v2.model.AttributeFilter;
import io.harness.resourcegroup.v2.model.ResourceSelector;
import io.harness.resourcegroup.v2.model.ScopeSelector;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupDTO;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupResponse;
import io.harness.rule.Owner;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.ResourceFilter;
import io.harness.spec.server.platform.model.ResourceGroupScope;
import io.harness.spec.server.platform.model.ResourceGroupsResponse;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(PL)

public class ResourceGroupApiImplTest extends CategoryTest {
  private ResourceGroupService resourceGroupService;
  private ResourceGroupValidatorImpl resourceGroupValidator;
  private AccountResourceGroupApiImpl accountResourceGroupApi;
  private OrgResourceGroupsApiImpl orgResourceGroupsApi;
  private ProjectResourceGroupsApiImpl projectResourceGroupsApi;

  String slug = randomAlphabetic(10);
  String name = randomAlphabetic(10);
  String account = randomAlphabetic(10);
  String org = randomAlphabetic(10);
  String project = randomAlphabetic(10);
  int page = 0;
  int limit = 1;

  @Before
  public void setup() {
    resourceGroupService = mock(ResourceGroupService.class);
    resourceGroupValidator = mock(ResourceGroupValidatorImpl.class);
    accountResourceGroupApi = new AccountResourceGroupApiImpl(resourceGroupService, resourceGroupValidator);
    orgResourceGroupsApi = new OrgResourceGroupsApiImpl(resourceGroupService, resourceGroupValidator);
    projectResourceGroupsApi = new ProjectResourceGroupsApiImpl(resourceGroupService, resourceGroupValidator);
  }

  @Test
  @Owner(developers = MANKRIT)
  @Category(UnitTests.class)
  public void testAccountScopedRGCreate() {
    ResourceGroupScope resourceGroupScope = new ResourceGroupScope();
    resourceGroupScope.setFilter(ResourceGroupScope.FilterEnum.EXCLUDING_CHILD_SCOPES);
    resourceGroupScope.setAccount(account);
    resourceGroupScope.setOrg(org);
    resourceGroupScope.setProject(project);
    List<ResourceGroupScope> includedScopes = Collections.singletonList(resourceGroupScope);

    ResourceFilter resourceFilter = new ResourceFilter();
    resourceFilter.setResourceType("RESOURCE");
    resourceFilter.setIdentifiers(Collections.singletonList("identifier"));
    resourceFilter.setAttributeName("name");
    resourceFilter.setAttributeValues(Collections.singletonList("resource1"));

    CreateResourceGroupRequest request = new CreateResourceGroupRequest();
    request.setSlug(slug);
    request.setName(name);
    request.setIncludedScope(includedScopes);
    request.setResourceFilter(Collections.singletonList(resourceFilter));
    request.setIncludeAll(false);

    doNothing().when(resourceGroupValidator).validateResourceGroup(any());

    ResourceGroupResponse resourceGroupResponse =
        ResourceGroupResponse.builder()
            .resourceGroup(
                ResourceGroupDTO.builder()
                    .accountIdentifier(account)
                    .identifier(slug)
                    .name(name)
                    .allowedScopeLevels(Collections.singleton("account"))
                    .includedScopes(Collections.singletonList(ScopeSelector.builder()
                                                                  .accountIdentifier(account)
                                                                  .orgIdentifier(org)
                                                                  .projectIdentifier(project)
                                                                  .filter(ScopeFilterType.EXCLUDING_CHILD_SCOPES)
                                                                  .build()))
                    .resourceFilter(
                        io.harness.resourcegroup.v2.model.ResourceFilter.builder()
                            .resources(Collections.singletonList(
                                ResourceSelector.builder()
                                    .resourceType("RESOURCE")
                                    .identifiers(Collections.singletonList("identifier"))
                                    .attributeFilter(AttributeFilter.builder()
                                                         .attributeName("name")
                                                         .attributeValues(Collections.singletonList("resource1"))
                                                         .build())
                                    .build()))
                            .includeAllResources(false)
                            .build())
                    .build())
            .build();
    when(resourceGroupService.create(any(ResourceGroupDTO.class), any(Boolean.class)))
        .thenReturn(resourceGroupResponse);

    Response response = accountResourceGroupApi.createResourceGroupAcc(request, account);
    ResourceGroupsResponse newResourceGroupResponse = (ResourceGroupsResponse) response.getEntity();
    assertEquals(slug, newResourceGroupResponse.getSlug());
    assertEquals(name, newResourceGroupResponse.getName());
    assertEquals(Collections.singletonList(ResourceGroupsResponse.AllowedScopeLevelsEnum.ACCOUNT),
        newResourceGroupResponse.getAllowedScopeLevels());
    assertEquals(includedScopes, newResourceGroupResponse.getIncludedScope());
    assertEquals(Collections.singletonList(resourceFilter), newResourceGroupResponse.getResourceFilter());
  }

  @Test
  @Owner(developers = MANKRIT)
  @Category(UnitTests.class)
  public void testOrgScopedRGCreate() {
    ResourceGroupScope resourceGroupScope = new ResourceGroupScope();
    resourceGroupScope.setFilter(ResourceGroupScope.FilterEnum.EXCLUDING_CHILD_SCOPES);
    resourceGroupScope.setAccount(account);
    resourceGroupScope.setOrg(org);
    resourceGroupScope.setProject(project);
    List<ResourceGroupScope> includedScopes = Collections.singletonList(resourceGroupScope);

    ResourceFilter resourceFilter = new ResourceFilter();
    resourceFilter.setResourceType("RESOURCE");
    resourceFilter.setIdentifiers(Collections.singletonList("identifier"));
    resourceFilter.setAttributeName("name");
    resourceFilter.setAttributeValues(Collections.singletonList("resource1"));

    CreateResourceGroupRequest request = new CreateResourceGroupRequest();
    request.setSlug(slug);
    request.setName(name);
    request.setIncludedScope(includedScopes);
    request.setResourceFilter(Collections.singletonList(resourceFilter));
    request.setIncludeAll(false);

    doNothing().when(resourceGroupValidator).validateResourceGroup(any());

    ResourceGroupResponse resourceGroupResponse =
        ResourceGroupResponse.builder()
            .resourceGroup(
                ResourceGroupDTO.builder()
                    .accountIdentifier(account)
                    .identifier(slug)
                    .name(name)
                    .allowedScopeLevels(Collections.singleton("organization"))
                    .includedScopes(Collections.singletonList(ScopeSelector.builder()
                                                                  .accountIdentifier(account)
                                                                  .orgIdentifier(org)
                                                                  .projectIdentifier(project)
                                                                  .filter(ScopeFilterType.EXCLUDING_CHILD_SCOPES)
                                                                  .build()))
                    .resourceFilter(
                        io.harness.resourcegroup.v2.model.ResourceFilter.builder()
                            .resources(Collections.singletonList(
                                ResourceSelector.builder()
                                    .resourceType("RESOURCE")
                                    .identifiers(Collections.singletonList("identifier"))
                                    .attributeFilter(AttributeFilter.builder()
                                                         .attributeName("name")
                                                         .attributeValues(Collections.singletonList("resource1"))
                                                         .build())
                                    .build()))
                            .includeAllResources(false)
                            .build())
                    .build())
            .build();
    when(resourceGroupService.create(any(ResourceGroupDTO.class), any(Boolean.class)))
        .thenReturn(resourceGroupResponse);

    Response response = orgResourceGroupsApi.createResourceGroupOrg(org, request, account);
    ResourceGroupsResponse newResourceGroupResponse = (ResourceGroupsResponse) response.getEntity();
    assertEquals(slug, newResourceGroupResponse.getSlug());
    assertEquals(name, newResourceGroupResponse.getName());
    assertEquals(Collections.singletonList(ResourceGroupsResponse.AllowedScopeLevelsEnum.ORGANIZATION),
        newResourceGroupResponse.getAllowedScopeLevels());
    assertEquals(includedScopes, newResourceGroupResponse.getIncludedScope());
    assertEquals(Collections.singletonList(resourceFilter), newResourceGroupResponse.getResourceFilter());
  }

  @Test
  @Owner(developers = MANKRIT)
  @Category(UnitTests.class)
  public void testProjectScopedRGCreate() {
    ResourceGroupScope resourceGroupScope = new ResourceGroupScope();
    resourceGroupScope.setFilter(ResourceGroupScope.FilterEnum.EXCLUDING_CHILD_SCOPES);
    resourceGroupScope.setAccount(account);
    resourceGroupScope.setOrg(org);
    resourceGroupScope.setProject(project);
    List<ResourceGroupScope> includedScopes = Collections.singletonList(resourceGroupScope);

    ResourceFilter resourceFilter = new ResourceFilter();
    resourceFilter.setResourceType("RESOURCE");
    resourceFilter.setIdentifiers(Collections.singletonList("identifier"));
    resourceFilter.setAttributeName("name");
    resourceFilter.setAttributeValues(Collections.singletonList("resource1"));

    CreateResourceGroupRequest request = new CreateResourceGroupRequest();
    request.setSlug(slug);
    request.setName(name);
    request.setIncludedScope(includedScopes);
    request.setResourceFilter(Collections.singletonList(resourceFilter));
    request.setIncludeAll(false);

    doNothing().when(resourceGroupValidator).validateResourceGroup(any());

    ResourceGroupResponse resourceGroupResponse =
        ResourceGroupResponse.builder()
            .resourceGroup(
                ResourceGroupDTO.builder()
                    .accountIdentifier(account)
                    .identifier(slug)
                    .name(name)
                    .allowedScopeLevels(Collections.singleton("project"))
                    .includedScopes(Collections.singletonList(ScopeSelector.builder()
                                                                  .accountIdentifier(account)
                                                                  .orgIdentifier(org)
                                                                  .projectIdentifier(project)
                                                                  .filter(ScopeFilterType.EXCLUDING_CHILD_SCOPES)
                                                                  .build()))
                    .resourceFilter(
                        io.harness.resourcegroup.v2.model.ResourceFilter.builder()
                            .resources(Collections.singletonList(
                                ResourceSelector.builder()
                                    .resourceType("RESOURCE")
                                    .identifiers(Collections.singletonList("identifier"))
                                    .attributeFilter(AttributeFilter.builder()
                                                         .attributeName("name")
                                                         .attributeValues(Collections.singletonList("resource1"))
                                                         .build())
                                    .build()))
                            .includeAllResources(false)
                            .build())
                    .build())
            .build();
    when(resourceGroupService.create(any(ResourceGroupDTO.class), any(Boolean.class)))
        .thenReturn(resourceGroupResponse);

    Response response = projectResourceGroupsApi.createResourceGroupProject(org, project, request, account);
    ResourceGroupsResponse newResourceGroupResponse = (ResourceGroupsResponse) response.getEntity();
    assertEquals(slug, newResourceGroupResponse.getSlug());
    assertEquals(name, newResourceGroupResponse.getName());
    assertEquals(Collections.singletonList(ResourceGroupsResponse.AllowedScopeLevelsEnum.PROJECT),
        newResourceGroupResponse.getAllowedScopeLevels());
    assertEquals(includedScopes, newResourceGroupResponse.getIncludedScope());
    assertEquals(Collections.singletonList(resourceFilter), newResourceGroupResponse.getResourceFilter());
  }
}
