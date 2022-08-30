package io.harness.resourcegroup.framework.v2.remote.resource;

import io.harness.spec.server.platform.OrganizationResourceGroupsApi;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.FilterResourceGroupRequest;

import javax.ws.rs.core.Response;

public class OrgResourceGroupsApiImpl implements OrganizationResourceGroupsApi {
  @Override
  public Response createResourceGroupOrg(String s, CreateResourceGroupRequest createResourceGroupRequest, String s1) {
    return null;
  }

  @Override
  public Response deleteResourceGroupOrg(String s, String s1, String s2) {
    return null;
  }

  @Override
  public Response filterResourceGroupOrg(
      String s, FilterResourceGroupRequest filterResourceGroupRequest, String s1, Integer integer, Integer integer1) {
    return null;
  }

  @Override
  public Response getResourceGroupInternalOrg(String s, String s1, String s2) {
    return null;
  }

  @Override
  public Response getResourceGroupOrg(String s, String s1, String s2) {
    return null;
  }

  @Override
  public Response listResourceGroupsOrg(String s, String s1, Integer integer, Integer integer1, String s2) {
    return null;
  }

  @Override
  public Response updateResourceGroupOrg(
      String s, String s1, CreateResourceGroupRequest createResourceGroupRequest, String s2) {
    return null;
  }
}
