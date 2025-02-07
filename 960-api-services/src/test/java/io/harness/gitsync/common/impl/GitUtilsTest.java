/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.gitsync.common.impl;

import static io.harness.rule.OwnerRule.MOHIT_GARG;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class GitUtilsTest extends CategoryTest {
  @Test
  @Owner(developers = MOHIT_GARG)
  @Category(UnitTests.class)
  public void convertToUrlWithGitTest() {
    String modifiedPath = GitUtils.convertToUrlWithGit("https://abc.git///");
    assertThat(modifiedPath).isEqualTo("https://abc");

    modifiedPath = GitUtils.convertToUrlWithGit("https://abc///");
    assertThat(modifiedPath).isEqualTo("https://abc");
  }
}
