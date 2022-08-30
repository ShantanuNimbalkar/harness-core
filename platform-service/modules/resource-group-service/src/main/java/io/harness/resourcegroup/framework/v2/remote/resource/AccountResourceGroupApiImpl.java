package io.harness.resourcegroup.framework.v2.remote.resource;

import io.harness.spec.server.platform.AccountResourceGroupsApi;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.FilterResourceGroupRequest;

import javax.ws.rs.core.Response;

public class AccountResourceGroupApiImpl implements AccountResourceGroupsApi {
  @Override
  public Response createResourceGroupAcc(CreateResourceGroupRequest createResourceGroupRequest, String s) {
    return null;
  }

  @Override
  public Response deleteResourceGroupAcc(String s, String s1) {
    return null;
  }

  @Override
  public Response filterResourceGroupAcc(
      FilterResourceGroupRequest filterResourceGroupRequest, String s, Integer integer, Integer integer1) {
    return null;
  }

  @Override
  public Response getResourceGroupAcc(String s, String s1) {
    return null;
  }

  @Override
  public Response getResourceGroupInternalAcc(String s, String s1) {
    return null;
  }

  @Override
  public Response listResourceGroupsAcc(String s, Integer integer, Integer integer1, String s1) {
    return null;
  }

  @Override
  public Response updateResourceGroupAcc(String s, CreateResourceGroupRequest createResourceGroupRequest, String s1) {
    return null;
  }
}
