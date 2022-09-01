package io.harness.resourcegroup.framework.v2.remote.resource;

import io.harness.spec.server.platform.ProjectResourceGroupsApi;
import io.harness.spec.server.platform.model.CreateResourceGroupRequest;
import io.harness.spec.server.platform.model.ResourceSelectorFilter;

import java.util.List;
import javax.ws.rs.core.Response;

public class ProjectResourceGroupsApiImpl implements ProjectResourceGroupsApi {
  @Override
  public Response createResourceGroupProject(
      String s, String s1, CreateResourceGroupRequest createResourceGroupRequest, String s2) {
    return null;
  }

  @Override
  public Response deleteResourceGroupProject(String s, String s1, String s2, String s3) {
    return null;
  }

  @Override
  public Response getResourceGroupProject(String s, String s1, String s2, String s3) {
    return null;
  }

  @Override
  public Response listResourceGroupsProject(String s, String s1, String s2, Integer integer, Integer integer1,
      String s3, List<String> list, List<ResourceSelectorFilter> list1, String s4) {
    return null;
  }

  @Override
  public Response updateResourceGroupProject(
      String s, String s1, String s2, CreateResourceGroupRequest createResourceGroupRequest, String s3) {
    return null;
  }
}
