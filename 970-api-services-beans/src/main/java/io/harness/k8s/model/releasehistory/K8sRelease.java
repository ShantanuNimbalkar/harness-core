/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.k8s.model.releasehistory;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.data.encoding.EncodingUtils.compressString;
import static io.harness.data.encoding.EncodingUtils.deCompressString;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_KEY;

import static java.util.Collections.emptyList;

import io.harness.annotations.dev.OwnedBy;
import io.harness.k8s.manifest.ManifestHelper;
import io.harness.k8s.model.KubernetesResource;
import io.harness.k8s.model.KubernetesResourceId;

import io.kubernetes.client.openapi.models.V1Secret;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@OwnedBy(CDP)
public class K8sRelease implements IK8sRelease {
  V1Secret releaseSecret;
  int releaseNumber;
  String releaseName;
  String status;

  @Override
  public Integer getReleaseNumber() {
    return releaseNumber;
  }

  @Override
  public List<KubernetesResource> getResourcesWithSpecs() {
    if (this.releaseSecret == null) {
      return emptyList();
    }

    try {
      Map<String, byte[]> secretData = releaseSecret.getData();
      if (secretData != null && secretData.containsKey(RELEASE_KEY)) {
        byte[] compressedYaml = secretData.get(RELEASE_KEY);
        String manifestsYaml = deCompressString(compressedYaml);
        return ManifestHelper.processYaml(manifestsYaml);
      }
    } catch (IOException ex) {
      log.error("Failed to extract resources from release.", ex);
    }
    return emptyList();
  }

  @Override
  public List<KubernetesResourceId> getResourceIds() {
    List<KubernetesResource> resources = getResourcesWithSpecs();
    return resources.stream().map(KubernetesResource::getResourceId).collect(Collectors.toList());
  }

  @Override
  public IK8sRelease setResourcesInRelease(List<KubernetesResource> resources) {
    try {
      String manifestsYaml = ManifestHelper.toYaml(resources);
      byte[] compressedYaml = compressString(manifestsYaml, Deflater.BEST_COMPRESSION);
      this.releaseSecret.setData(Map.of(RELEASE_KEY, compressedYaml));
    } catch (IOException e) {
      log.error("Failed to set resources in release", e);
    }
    return this;
  }
}
