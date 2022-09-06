/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.helpers.k8s.releasehistory;

import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_HARNESS_SECRET_TYPE;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_NUMBER_LABEL_KEY;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_SECRET_TYPE_VALUE;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_STATUS_LABEL_KEY;

import io.harness.data.structure.EmptyPredicate;
import io.harness.k8s.KubernetesContainerService;
import io.harness.k8s.model.K8sLegacyRelease;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.releasehistory.IK8sRelease;
import io.harness.k8s.model.releasehistory.IK8sReleaseHistory;
import io.harness.k8s.model.releasehistory.K8sRelease;
import io.harness.k8s.model.releasehistory.K8sReleaseHistory;
import io.harness.k8s.model.releasehistory.K8sReleasePersistDTO;

import com.google.inject.Inject;
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class K8sReleaseHandlerImpl implements K8sReleaseHandler {
  @Inject KubernetesContainerService kubernetesContainerService;
  @Inject K8sReleaseHelper releaseHelper;

  @Override
  public IK8sReleaseHistory getReleaseHistory(KubernetesConfig kubernetesConfig, String releaseName) {
    Map<String, String> labels = releaseHelper.createLabelsMap(releaseName);

    String labelArg = releaseHelper.createCommaSeparatedKeyValueList(labels);
    String fieldArg = releaseHelper.createCommaSeparatedKeyValueList(RELEASE_HARNESS_SECRET_TYPE);
    List<V1Secret> releaseSecrets =
        kubernetesContainerService.getSecretsWithLabelsAndFields(kubernetesConfig, labelArg, fieldArg);
    List<K8sRelease> releases = createReleasesFromSecrets(releaseSecrets);

    return K8sReleaseHistory.builder().releaseHistory(releases).build();
  }

  @Override
  public IK8sRelease createRelease(String name, int number) {
    String status = K8sLegacyRelease.Status.InProgress.name();
    String generatedReleaseName = releaseHelper.generateName(name, number);
    Map<String, String> labels = releaseHelper.generateLabels(name, number, status);
    V1Secret releaseSecret =
        new V1SecretBuilder()
            .withMetadata(new V1ObjectMetaBuilder().withName(generatedReleaseName).withLabels(labels).build())
            .withType(RELEASE_SECRET_TYPE_VALUE)
            .build();

    return K8sRelease.builder()
        .releaseSecret(releaseSecret)
        .releaseName(generatedReleaseName)
        .releaseNumber(number)
        .status(status)
        .build();
  }

  @Override
  public void saveRelease(KubernetesConfig kubernetesConfig, K8sReleasePersistDTO releasePersistDTO) throws Exception {
    K8sRelease releaseSecret = (K8sRelease) releasePersistDTO.getRelease();
    kubernetesContainerService.createOrReplaceSecret(kubernetesConfig, releaseSecret.getReleaseSecret());
  }

  private List<K8sRelease> createReleasesFromSecrets(List<V1Secret> releaseSecrets) {
    return releaseSecrets.stream()
        .map(releaseSecret
            -> K8sRelease.builder()
                   .releaseSecret(releaseSecret)
                   .releaseNumber(getReleaseNumberFromRelease(releaseSecret))
                   .status(releaseHelper.getReleaseLabelValue(releaseSecret, RELEASE_STATUS_LABEL_KEY))
                   .build())
        .collect(Collectors.toList());
  }

  private Integer getReleaseNumberFromRelease(V1Secret release) {
    String releaseNumberLabelValue = releaseHelper.getReleaseLabelValue(release, RELEASE_NUMBER_LABEL_KEY);
    if (EmptyPredicate.isNotEmpty(releaseNumberLabelValue)) {
      return Integer.parseInt(releaseNumberLabelValue);
    }
    return -1;
  }
}
