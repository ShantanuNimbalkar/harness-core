/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.batch.processing.ccm;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class InstanceFamilyAndRegion {
  String instanceFamily;
  String region;

  @Override
  public int hashCode() {
    return Objects.hashCode(instanceFamily, region);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InstanceFamilyAndRegion that = (InstanceFamilyAndRegion) o;

    return Objects.equal(instanceFamily, that.instanceFamily) && Objects.equal(region, that.region);
  }

  @Override
  public String toString() {
    return String.format("('%s', '%s')", instanceFamily, region);
  }
}
