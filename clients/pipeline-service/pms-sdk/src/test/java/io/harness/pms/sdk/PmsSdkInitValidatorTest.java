/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.sdk;

import static io.harness.pms.sdk.PmsSdkInitValidator.supportedTypesP;
import static io.harness.pms.sdk.PmsSdkInitValidator.validatePlanCreators;
import static io.harness.rule.OwnerRule.FERNANDOD;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.harness.category.element.UnitTests;
import io.harness.pms.sdk.core.pipeline.filters.FilterJsonCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PipelineServiceInfoProvider;
import io.harness.pms.sdk.core.variables.VariableCreator;
import io.harness.rule.Owner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PmsSdkInitValidatorTest {
  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenFilterHasSameSize() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "queue"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email", "http"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "policy", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);

    assertThat(result.getLeft()).hasSize(1);
    assertThat(result.getLeft().get("step")).containsOnly("http");
    assertThat(result.getRight()).isEmpty();
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenFilterHasLessElements() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "queue"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "policy", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);

    assertThat(result.getLeft()).hasSize(1);
    assertThat(result.getLeft().get("step")).containsOnly("queue");
    assertThat(result.getRight()).isEmpty();
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenFilterHasMoreElements() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "queue"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email", "http", "queue"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "policy", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);
    assertThat(result.getLeft()).hasSize(1);
    assertThat(result.getLeft().get("step")).containsOnly("http");
    assertThat(result.getRight()).isEmpty();
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenVariableHasSameSize() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "http"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email", "http"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "policy", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);
    assertThat(result.getLeft()).isEmpty();
    assertThat(result.getRight()).hasSize(1);
    assertThat(result.getRight().get("step")).containsOnly("queue");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenVariableHasLessElements() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "http"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email", "http"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);
    assertThat(result.getLeft()).isEmpty();
    assertThat(result.getRight()).hasSize(1);
    assertThat(result.getRight().get("step")).containsOnly("policy", "http");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorWhenVariableHasMoreElements() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "policy", "email", "http"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "policy", "email", "http"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "email", "queue", "policy", "http"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);
    assertThat(result.getLeft()).isEmpty();
    assertThat(result.getRight()).hasSize(1);
    assertThat(result.getRight().get("step")).containsOnly("queue");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidatePlanCreatorAndDetectMissingIdentifiers() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("step", "email", "jira", "http"));
    planCreators.add(createPlanCreator("pipeline", "__any__"));
    planCreators.add(createPlanCreator("stepGroup", "email", "http"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("step", "email", "jira"));
    filterCreators.add(createFilterCreator("stepGroup", "email"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("step", "policy", "email", "queue"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Pair<Map<String, Set<String>>, Map<String, Set<String>>> result = validatePlanCreators(
        supportedTypesP(pipelineServiceInfoProvider.getPlanCreators()), pipelineServiceInfoProvider);
    assertThat(result.getLeft()).hasSize(3);
    assertThat(result.getLeft()).containsKeys("step", "pipeline", "stepGroup");
    assertThat(result.getLeft().get("step")).containsOnly("http");
    assertThat(result.getLeft().get("pipeline")).isEmpty();
    assertThat(result.getLeft().get("stepGroup")).containsOnly("http");
    assertThat(result.getRight()).hasSize(3);
    assertThat(result.getRight().get("step")).containsOnly("queue", "policy");
    assertThat(result.getRight().get("pipeline")).isEmpty();
    assertThat(result.getRight().get("stepGroup")).isEmpty();
  }

  // --
  // PRIVATE METHODS

  private PartialPlanCreator<?> createPlanCreator(String name, String... elements) {
    PartialPlanCreator<?> creator = mock(PartialPlanCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(name, elements));
    return creator;
  }

  private FilterJsonCreator<?> createFilterCreator(String name, String... elements) {
    FilterJsonCreator<?> creator = mock(FilterJsonCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(name, elements));
    return creator;
  }

  private VariableCreator<?> createVariableCreator(String name, String... elements) {
    VariableCreator<?> creator = mock(VariableCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(name, elements));
    return creator;
  }

  private Map<String, Set<String>> createSupportedTypes(String name, String... elements) {
    Map<String, Set<String>> supportedTypes = new HashMap<>();
    supportedTypes.put(name, new HashSet(Arrays.asList(elements)));
    return supportedTypes;
  }

  @NotNull
  private PipelineServiceInfoProvider createPipelineServiceInfoProvider(List<PartialPlanCreator<?>> planCreators,
      List<FilterJsonCreator> filterCreators, List<VariableCreator> variableCreators) {
    PipelineServiceInfoProvider pipelineServiceInfoProvider = mock(PipelineServiceInfoProvider.class);
    when(pipelineServiceInfoProvider.getPlanCreators()).thenReturn(planCreators);
    when(pipelineServiceInfoProvider.getFilterJsonCreators()).thenReturn(filterCreators);
    when(pipelineServiceInfoProvider.getVariableCreators()).thenReturn(variableCreators);
    return pipelineServiceInfoProvider;
  }
}
