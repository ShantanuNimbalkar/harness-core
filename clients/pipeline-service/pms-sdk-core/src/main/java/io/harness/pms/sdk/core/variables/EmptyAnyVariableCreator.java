/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.sdk.core.variables;

import static io.harness.pms.plan.creation.PlanCreatorUtils.ANY_TYPE;

import io.harness.pms.sdk.core.variables.beans.VariableCreationContext;
import io.harness.pms.sdk.core.variables.beans.VariableCreationResponse;
import io.harness.pms.yaml.YamlField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EmptyAnyVariableCreator extends ChildrenVariableCreator<YamlField> {

  private final Set<String> types;

  public EmptyAnyVariableCreator(Set<String> types) {
    if (CollectionUtils.isEmpty(types)) {
      throw new IllegalArgumentException("Type list cannot be null or empty");
    }
    this.types = types;
  }

  @Override
  public LinkedHashMap<String, VariableCreationResponse> createVariablesForChildrenNodes(
      VariableCreationContext ctx, YamlField config) {
    return new LinkedHashMap<>();
  }

  @Override
  public VariableCreationResponse createVariablesForParentNode(VariableCreationContext ctx, YamlField config) {
    return VariableCreationResponse.builder().build();
  }

  @Override
  public Map<String, Set<String>> getSupportedTypes() {
    Map<String, Set<String>> supportedTypes = new HashMap<>();
    types.forEach(type -> supportedTypes.put(type, Collections.singleton(ANY_TYPE)));
    return supportedTypes;
  }
}
