/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.helpers.k8s.releasehistory;

import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.delegate.task.k8s.K8sTaskHelperBase;
import io.harness.k8s.model.K8sLegacyRelease;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.ReleaseHistory;
import io.harness.k8s.model.releasehistory.IK8sRelease;
import io.harness.k8s.model.releasehistory.IK8sReleaseHistory;
import io.harness.k8s.model.releasehistory.K8SLegacyReleaseHistory;
import io.harness.k8s.model.releasehistory.K8sReleasePersistDTO;

import com.google.inject.Inject;

public class K8sLegacyReleaseHandlerImpl implements K8sReleaseHandler {
  @Inject K8sTaskHelperBase k8sTaskHelperBase;

  @Override
  public IK8sReleaseHistory getReleaseHistory(KubernetesConfig kubernetesConfig, String releaseName) throws Exception {
    String releaseHistoryData = k8sTaskHelperBase.getReleaseHistoryData(kubernetesConfig, releaseName);
    ReleaseHistory releaseHistory =
        (isEmpty(releaseHistoryData)) ? ReleaseHistory.createNew() : ReleaseHistory.createFromData(releaseHistoryData);
    return K8SLegacyReleaseHistory.builder().releaseHistory(releaseHistory).build();
  }

  @Override
  public IK8sRelease createRelease(String name, int number) {
    return K8sLegacyRelease.builder().number(number).status(K8sLegacyRelease.Status.InProgress).build();
  }

  @Override
  public void saveRelease(KubernetesConfig kubernetesConfig, K8sReleasePersistDTO releasePersistDTO) throws Exception {
    k8sTaskHelperBase.saveReleaseHistory(kubernetesConfig, releasePersistDTO.getReleaseName(),
        releasePersistDTO.getReleaseHistoryYaml(), releasePersistDTO.isStoreInSecrets());
  }
}
