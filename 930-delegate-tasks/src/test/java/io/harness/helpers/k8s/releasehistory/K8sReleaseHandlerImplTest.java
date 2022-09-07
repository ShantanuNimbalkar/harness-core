/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.helpers.k8s.releasehistory;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_HARNESS_SECRET_LABELS;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_HARNESS_SECRET_TYPE;
import static io.harness.k8s.model.releasehistory.K8sReleaseConstants.RELEASE_KEY;
import static io.harness.rule.OwnerRule.ABHINAV2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.k8s.KubernetesContainerService;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.releasehistory.K8sRelease;
import io.harness.k8s.model.releasehistory.K8sReleasePersistDTO;
import io.harness.rule.Owner;

import io.kubernetes.client.openapi.models.V1Secret;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(CDP)
public class K8sReleaseHandlerImplTest extends CategoryTest {
  @Mock KubernetesContainerService kubernetesContainerService;
  @Mock K8sReleaseHelper releaseHelper;

  @InjectMocks K8sReleaseHandlerImpl releaseHandler;

  private static final String RELEASE_NAME = "releaseName";

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testGetReleaseHistory() {
    Map<String, String> labels = new HashMap<>(RELEASE_HARNESS_SECRET_LABELS);
    labels.put(RELEASE_KEY, RELEASE_NAME);

    doReturn("labelArg").when(releaseHelper).createCommaSeparatedKeyValueList(labels);
    doReturn("fieldArg").when(releaseHelper).createCommaSeparatedKeyValueList(RELEASE_HARNESS_SECRET_TYPE);

    releaseHandler.getReleaseHistory(KubernetesConfig.builder().build(), RELEASE_NAME);
    verify(kubernetesContainerService).getSecretsWithLabelsAndFields(any(), eq("labelArg"), eq("fieldArg"));
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testCreateRelease() {
    doReturn(RELEASE_NAME).when(releaseHelper).generateName(anyString(), anyInt());
    K8sRelease release = (K8sRelease) releaseHandler.createRelease("name", 1);
    assertThat(release.getReleaseName()).isEqualTo(RELEASE_NAME);
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testSaveRelease() throws Exception {
    doReturn(null).when(kubernetesContainerService).createOrReplaceSecret(any(), any());
    V1Secret release = new V1Secret();
    KubernetesConfig kubernetesConfig = KubernetesConfig.builder().build();
    releaseHandler.saveRelease(kubernetesConfig,
        K8sReleasePersistDTO.builder().release(K8sRelease.builder().releaseSecret(release).build()).build());
    verify(kubernetesContainerService).createOrReplaceSecret(kubernetesConfig, release);
  }
}
