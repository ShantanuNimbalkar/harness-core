/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */
package io.harness.resourcegroup.framework.v2.remote.resource;

import static io.harness.resourcegroup.ResourceGroupPermissions.*;
import static io.harness.resourcegroup.ResourceGroupResourceTypes.RESOURCE_GROUP;

import io.harness.accesscontrol.NGAccessControlCheck;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.enforcement.client.annotation.FeatureRestrictionCheck;
import io.harness.enforcement.constants.FeatureRestrictionName;
import io.harness.resourcegroup.framework.v2.service.ResourceGroupService;
import io.harness.resourcegroup.framework.v2.service.impl.ResourceGroupValidatorImpl;
import io.harness.resourcegroup.v2.remote.dto.ResourceGroupRequest;
import io.harness.security.annotations.NextGenManagerAuth;
import io.harness.spec.server.platform.ProjectResourceGroupsApi;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.ResourceGroupsResponse;
import io.harness.spec.server.platform.model.ResourceSelectorFilter;

import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PUBLIC, onConstructor = @__({ @Inject }))
@NextGenManagerAuth
@OwnedBy(HarnessTeam.PL)
public class ProjectResourceGroupsApiImpl implements ProjectResourceGroupsApi {
  ResourceGroupService resourceGroupService;
  ResourceGroupValidatorImpl resourceGroupValidator;

  @Override
  @NGAccessControlCheck(resourceType = RESOURCE_GROUP, permission = EDIT_RESOURCEGROUP_PERMISSION)
  @FeatureRestrictionCheck(FeatureRestrictionName.CUSTOM_RESOURCE_GROUPS)
  public Response createResourceGroupProject(
      String org, String project, CreateResourceGroupRequest body, String account) {
    ResourceGroupRequest resourceGroupRequest =
        ResourceGroupApiUtils.getResourceGroupRequestProject(org, project, body, account);
    resourceGroupValidator.validateResourceGroup(resourceGroupRequest);
    ResourceGroupsResponse resourceGroupsResponse = ResourceGroupApiUtils.getResourceGroupResponse(
        resourceGroupService.create(resourceGroupRequest.getResourceGroup(), false));
    return Response.ok().entity(resourceGroupsResponse).build();
  }

  @Override
  @NGAccessControlCheck(resourceType = RESOURCE_GROUP, permission = DELETE_RESOURCEGROUP_PERMISSION)
  public Response deleteResourceGroupProject(String s, String s1, String s2, String s3) {
    return null;
  }

  @Override
  @NGAccessControlCheck(resourceType = RESOURCE_GROUP, permission = VIEW_RESOURCEGROUP_PERMISSION)
  public Response getResourceGroupProject(String s, String s1, String s2, String s3) {
    return null;
  }

  @Override
  @NGAccessControlCheck(resourceType = RESOURCE_GROUP, permission = VIEW_RESOURCEGROUP_PERMISSION)
  public Response listResourceGroupsProject(String s, String s1, String s2, Integer integer, Integer integer1,
      String s3, List<String> list, List<ResourceSelectorFilter> list1, String s4) {
    return null;
  }

  @Override
  @NGAccessControlCheck(resourceType = RESOURCE_GROUP, permission = EDIT_RESOURCEGROUP_PERMISSION)
  public Response updateResourceGroupProject(
      String s, String s1, String s2, CreateResourceGroupRequest createResourceGroupRequest, String s3) {
    return null;
  }
}
