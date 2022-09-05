/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */
package io.harness.resourcegroup.framework.v3.mapper;

import io.harness.ng.beans.PageRequest;
import io.harness.resourcegroup.beans.ScopeFilterType;
import io.harness.resourcegroup.v1.remote.dto.ManagedFilter;
import io.harness.resourcegroup.v1.remote.dto.ResourceGroupFilterDTO;
import io.harness.resourcegroup.v1.remote.dto.ResourceSelectorFilter;
import io.harness.resourcegroup.v2.model.AttributeFilter;
import io.harness.resourcegroup.v2.model.ResourceFilter;
import io.harness.resourcegroup.v2.model.ResourceSelector;
import io.harness.resourcegroup.v2.model.ScopeSelector;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupDTO;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupRequest;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupResponse;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.ResourceGroupScope;
import io.harness.spec.server.platform.model.ResourceGroupsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

public class ResourceGroupApiUtils {
  public static ResourceGroupRequest getResourceGroupRequestAcc(CreateResourceGroupRequest body, String account) {
    return ResourceGroupRequest.builder()
        .resourceGroup(ResourceGroupDTO.builder()
                           .accountIdentifier(account)
                           .identifier(body.getSlug())
                           .name(body.getName())
                           .color(body.getColor())
                           .tags(body.getTags())
                           .description(body.getDescription())
                           .allowedScopeLevels(Collections.singleton("account"))
                           .includedScopes(getIncludedScopeRequest(body.getIncludedScope()))
                           .resourceFilter(getResourceFilterRequest(body.getResourceFilter(), body.isIncludeAll()))
                           .build())
        .build();
  }
  public static ResourceGroupRequest getResourceGroupRequestOrg(
      String org, CreateResourceGroupRequest body, String account) {
    return ResourceGroupRequest.builder()
        .resourceGroup(ResourceGroupDTO.builder()
                           .accountIdentifier(account)
                           .orgIdentifier(org)
                           .identifier(body.getSlug())
                           .name(body.getName())
                           .color(body.getColor())
                           .tags(body.getTags())
                           .description(body.getDescription())
                           .allowedScopeLevels(Collections.singleton("organization"))
                           .includedScopes(getIncludedScopeRequest(body.getIncludedScope()))
                           .resourceFilter(getResourceFilterRequest(body.getResourceFilter(), body.isIncludeAll()))
                           .build())
        .build();
  }
  public static ResourceGroupRequest getResourceGroupRequestProject(
      String org, String project, CreateResourceGroupRequest body, String account) {
    return ResourceGroupRequest.builder()
        .resourceGroup(ResourceGroupDTO.builder()
                           .accountIdentifier(account)
                           .orgIdentifier(org)
                           .projectIdentifier(project)
                           .identifier(body.getSlug())
                           .name(body.getName())
                           .color(body.getColor())
                           .tags(body.getTags())
                           .description(body.getDescription())
                           .allowedScopeLevels(Collections.singleton("project"))
                           .includedScopes(getIncludedScopeRequest(body.getIncludedScope()))
                           .resourceFilter(getResourceFilterRequest(body.getResourceFilter(), body.isIncludeAll()))
                           .build())
        .build();
  }
  public static List<ScopeSelector> getIncludedScopeRequest(List<ResourceGroupScope> resourceGroupScopes) {
    return resourceGroupScopes.stream()
        .map(scope
            -> ScopeSelector.builder()
                   .accountIdentifier(scope.getAccount())
                   .orgIdentifier(scope.getOrg())
                   .projectIdentifier(scope.getProject())
                   .filter((scope.getFilter()).equals(ResourceGroupScope.FilterEnum.INCLUDING_CHILD_SCOPES)
                           ? ScopeFilterType.INCLUDING_CHILD_SCOPES
                           : ScopeFilterType.EXCLUDING_CHILD_SCOPES)
                   .build())
        .collect(Collectors.toList());
  }

  public static ResourceFilter getResourceFilterRequest(
      List<io.harness.spec.server.platform.model.ResourceFilter> filters, boolean includeAll) {
    return ResourceFilter.builder()
        .resources(filters.stream().map(ResourceGroupApiUtils::getFilterRequest).collect(Collectors.toList()))
        .includeAllResources(includeAll)
        .build();
  }

  public static ResourceSelector getFilterRequest(io.harness.spec.server.platform.model.ResourceFilter filter) {
    return ResourceSelector.builder()
        .resourceType(filter.getResourceType())
        .identifiers(filter.getIdentifiers())
        .attributeFilter(AttributeFilter.builder()
                             .attributeName(filter.getAttributeName())
                             .attributeValues(filter.getAttributeValues())
                             .build())
        .build();
  }

  public static io.harness.spec.server.platform.model.ResourceGroupsResponse getResourceGroupResponse(
      ResourceGroupResponse response) {
    if (response == null) {
      return null;
    }
    ResourceGroupsResponse resourceGroupsResponse = new ResourceGroupsResponse();
    resourceGroupsResponse.setSlug(response.getResourceGroup().getIdentifier());
    resourceGroupsResponse.setName(response.getResourceGroup().getName());
    resourceGroupsResponse.setColor(response.getResourceGroup().getColor());
    resourceGroupsResponse.setTags(response.getResourceGroup().getTags());
    resourceGroupsResponse.setDescription(response.getResourceGroup().getDescription());
    resourceGroupsResponse.setAllowedScopeLevels(response.getResourceGroup()
                                                     .getAllowedScopeLevels()
                                                     .stream()
                                                     .map(ResourceGroupApiUtils::getAllowedScopeEnum)
                                                     .collect(Collectors.toList()));
    resourceGroupsResponse.setIncludedScope(response.getResourceGroup()
                                                .getIncludedScopes()
                                                .stream()
                                                .map(ResourceGroupApiUtils::getIncludedScopeResponse)
                                                .collect(Collectors.toList()));
    resourceGroupsResponse.setResourceFilter(response.getResourceGroup()
                                                 .getResourceFilter()
                                                 .getResources()
                                                 .stream()
                                                 .map(ResourceGroupApiUtils::getResourceFilterResponse)
                                                 .collect(Collectors.toList()));
    resourceGroupsResponse.setIncludeAll(response.getResourceGroup().getResourceFilter().isIncludeAllResources());
    resourceGroupsResponse.setCreated(response.getCreatedAt());
    resourceGroupsResponse.setUpdated(response.getLastModifiedAt());
    resourceGroupsResponse.setHarnessManaged(response.isHarnessManaged());
    return resourceGroupsResponse;
  }

  public static ResourceGroupsResponse.AllowedScopeLevelsEnum getAllowedScopeEnum(String scope) {
    if (scope.equals("account")) {
      return ResourceGroupsResponse.AllowedScopeLevelsEnum.ACCOUNT;
    }
    if (scope.equals("organization")) {
      return ResourceGroupsResponse.AllowedScopeLevelsEnum.ORGANIZATION;
    }
    return ResourceGroupsResponse.AllowedScopeLevelsEnum.PROJECT;
  }

  public static ResourceGroupScope getIncludedScopeResponse(ScopeSelector includedScopes) {
    ResourceGroupScope resourceGroupScope = new ResourceGroupScope();
    resourceGroupScope.setAccount(includedScopes.getAccountIdentifier());
    resourceGroupScope.setOrg(includedScopes.getOrgIdentifier());
    resourceGroupScope.setProject(includedScopes.getProjectIdentifier());
    resourceGroupScope.setFilter((includedScopes.getFilter().equals(ScopeFilterType.INCLUDING_CHILD_SCOPES))
            ? ResourceGroupScope.FilterEnum.INCLUDING_CHILD_SCOPES
            : ResourceGroupScope.FilterEnum.EXCLUDING_CHILD_SCOPES);
    return resourceGroupScope;
  }

  public static io.harness.spec.server.platform.model.ResourceFilter getResourceFilterResponse(
      ResourceSelector resourceSelector) {
    io.harness.spec.server.platform.model.ResourceFilter resourceFilter =
        new io.harness.spec.server.platform.model.ResourceFilter();
    resourceFilter.setResourceType(resourceSelector.getResourceType());
    resourceFilter.setIdentifiers(resourceSelector.getIdentifiers());
    resourceFilter.setAttributeName(resourceSelector.getAttributeFilter().getAttributeName());
    resourceFilter.setAttributeValues(resourceSelector.getAttributeFilter().getAttributeValues());
    return resourceFilter;
  }

  public static ResourceGroupFilterDTO getResourceFilterDTO(String account, String org, String project,
      String searchTerm, List<String> identifierFilter, String managedFilter, List<String> resourceTypeFilter,
      List<String> resourceSlugFilter) {
    return ResourceGroupFilterDTO.builder()
        .accountIdentifier(account)
        .orgIdentifier(org)
        .projectIdentifier(project)
        .searchTerm(searchTerm)
        .identifierFilter(new HashSet<>(identifierFilter))
        .resourceSelectorFilterList(getResourceSelectorFilter(resourceTypeFilter, resourceSlugFilter))
        .managedFilter(getManagedFilter(managedFilter))
        .build();
  }

  public static Set<ResourceSelectorFilter> getResourceSelectorFilter(
      List<String> resourceTypeFilter, List<String> resourceSlugFilter) {
    int size = resourceSlugFilter.size();
    Set<ResourceSelectorFilter> resourceSelectorFilter = new HashSet<>();
    for (int i = 0; i < size; i++) {
      resourceSelectorFilter.add(ResourceSelectorFilter.builder()
                                     .resourceType(resourceTypeFilter.get(i))
                                     .resourceIdentifier(resourceSlugFilter.get(i))
                                     .build());
    }
    return resourceSelectorFilter;
  }

  public static ManagedFilter getManagedFilter(String managedFilter) {
    if (managedFilter == null) {
      return null;
    }
    if (managedFilter.equals("NO_FILTER")) {
      return ManagedFilter.NO_FILTER;
    }
    if (managedFilter.equals("ONLY_MANAGED")) {
      return ManagedFilter.ONLY_MANAGED;
    }
    return ManagedFilter.ONLY_CUSTOM;
  }

  public static PageRequest getPageRequest(Integer page, Integer limit) {
    return PageRequest.builder().pageIndex(page).pageSize(limit).build();
  }

  public static ResponseBuilder addLinksHeader(
      ResponseBuilder responseBuilder, String path, int currentResultCount, int page, int limit) {
    ArrayList<Link> links = new ArrayList();
    links.add(Link.fromUri(UriBuilder.fromPath(path)
                               .queryParam("page", new Object[] {page})
                               .queryParam("page_size", new Object[] {limit})
                               .build(new Object[0]))
                  .rel("self")
                  .build(new Object[0]));
    if (page >= 1) {
      links.add(Link.fromUri(UriBuilder.fromPath(path)
                                 .queryParam("page", new Object[] {page - 1})
                                 .queryParam("page_size", new Object[] {limit})
                                 .build(new Object[0]))
                    .rel("previous")
                    .build(new Object[0]));
    }

    if (limit == currentResultCount) {
      links.add(Link.fromUri(UriBuilder.fromPath(path)
                                 .queryParam("page", new Object[] {page + 1})
                                 .queryParam("page_size", new Object[] {limit})
                                 .build(new Object[0]))
                    .rel("next")
                    .build(new Object[0]));
    }

    return responseBuilder.links((Link[]) links.toArray(new Link[links.size()]));
  }
}
