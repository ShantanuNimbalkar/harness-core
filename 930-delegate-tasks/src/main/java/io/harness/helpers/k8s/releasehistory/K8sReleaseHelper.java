/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.helpers.k8s.releasehistory;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_KEY;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_LABEL_QUERY_LIST_FORMAT;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_LABEL_QUERY_SET_FORMAT;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_NAME_DELIMITER;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_NUMBER_LABEL_KEY;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_OWNER_LABEL_KEY;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_OWNER_LABEL_VALUE;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_SECRET_LABELS_MAP;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_STATUS_LABEL_KEY;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.SECRET_LABEL_DELIMITER;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import io.harness.annotations.dev.OwnedBy;

import com.google.inject.Singleton;
import io.kubernetes.client.openapi.models.V1Secret;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@OwnedBy(CDP)
public class K8sReleaseHelper {
  String createSetBasedArg(String key, Set<String> values) {
    return String.format(RELEASE_LABEL_QUERY_SET_FORMAT, key, String.join(SECRET_LABEL_DELIMITER, values));
  }

  String createListBasedArg(String key, String value) {
    return String.format(RELEASE_LABEL_QUERY_LIST_FORMAT, key, value);
  }

  String createCommaSeparatedKeyValueList(Map<String, String> k8sArg) {
    return k8sArg.entrySet()
        .stream()
        .map(entry -> createListBasedArg(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(SECRET_LABEL_DELIMITER));
  }

  String generateName(String releaseName, int releaseNumber) {
    return String.join(
        RELEASE_KEY, RELEASE_NAME_DELIMITER, releaseName, RELEASE_NAME_DELIMITER, String.valueOf(releaseNumber));
  }

  Map<String, String> generateLabels(String releaseName, int releaseNumber, String status) {
    return Map.of(RELEASE_KEY, releaseName, RELEASE_NUMBER_LABEL_KEY, String.valueOf(releaseNumber),
        RELEASE_OWNER_LABEL_KEY, RELEASE_OWNER_LABEL_VALUE, RELEASE_STATUS_LABEL_KEY, status);
  }

  Map<String, String> createLabelsMap(String releaseName) {
    Map<String, String> labels = new HashMap<>(RELEASE_SECRET_LABELS_MAP);
    labels.put(RELEASE_KEY, releaseName);
    return labels;
  }

  public String getReleaseLabelValue(V1Secret release, String labelKey) {
    if (release != null && release.getMetadata() != null && release.getMetadata().getLabels() != null
        && release.getMetadata().getLabels().containsKey(labelKey)) {
      return release.getMetadata().getLabels().get(labelKey);
    }
    return EMPTY;
  }
}
