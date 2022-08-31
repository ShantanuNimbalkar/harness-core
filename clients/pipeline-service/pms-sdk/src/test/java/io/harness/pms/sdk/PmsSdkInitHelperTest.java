/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.sdk;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.pms.contracts.plan.ExpansionRequestType.KEY;
import static io.harness.pms.contracts.plan.ExpansionRequestType.LOCAL_FQN;
import static io.harness.rule.OwnerRule.FERNANDOD;
import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.ModuleType;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.governance.ExpansionRequestMetadata;
import io.harness.pms.contracts.plan.JsonExpansionInfo;
import io.harness.pms.contracts.plan.Types;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.execution.expression.SdkFunctor;
import io.harness.pms.sdk.core.governance.ExpansionResponse;
import io.harness.pms.sdk.core.governance.JsonExpansionHandler;
import io.harness.pms.sdk.core.governance.JsonExpansionHandlerInfo;
import io.harness.pms.sdk.core.pipeline.filters.FilterJsonCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PipelineServiceInfoProvider;
import io.harness.pms.sdk.core.variables.VariableCreator;
import io.harness.rule.Owner;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(PIPELINE)
public class PmsSdkInitHelperTest extends CategoryTest {
  // VALIDATION OF SUPPORTED TYPES DON'T CARE ABOUT THE TYPE.
  // THE CHECK IS DONE BY KEY MAP, NOT THE VALUE.
  private static final Set<String> DEFAULT_EMPTY_TYPE_CONTENT = ImmutableSet.of("a1", "b2", "c3");

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testGetJsonExpansionInfo() {
    PmsSdkConfiguration sdkConfigurationWithNull = PmsSdkConfiguration.builder().moduleType(ModuleType.CD).build();
    assertThat(PmsSdkInitHelper.getJsonExpansionInfo(sdkConfigurationWithNull)).isEmpty();
    PmsSdkConfiguration sdkConfigurationWithEmpty =
        PmsSdkConfiguration.builder().moduleType(ModuleType.CD).jsonExpansionHandlers(Collections.emptyList()).build();
    assertThat(PmsSdkInitHelper.getJsonExpansionInfo(sdkConfigurationWithEmpty)).isEmpty();

    List<JsonExpansionHandlerInfo> jsonExpansionHandlers = new ArrayList<>();
    JsonExpansionHandlerInfo connectorRefExpansionHandlerInfo =
        JsonExpansionHandlerInfo.builder()
            .jsonExpansionInfo(JsonExpansionInfo.newBuilder().setKey("connectorRef").setExpansionType(KEY).build())
            .expansionHandler(Dummy1.class)
            .build();
    jsonExpansionHandlers.add(connectorRefExpansionHandlerInfo);
    JsonExpansionHandlerInfo abcExpansionHandlerInfo =
        JsonExpansionHandlerInfo.builder()
            .jsonExpansionInfo(JsonExpansionInfo.newBuilder().setKey("abc").setExpansionType(KEY).build())
            .expansionHandler(Dummy1.class)
            .build();
    jsonExpansionHandlers.add(abcExpansionHandlerInfo);
    JsonExpansionHandlerInfo defExpansionHandlerInfo =
        JsonExpansionHandlerInfo.builder()
            .jsonExpansionInfo(JsonExpansionInfo.newBuilder()
                                   .setKey("def")
                                   .setExpansionType(LOCAL_FQN)
                                   .setStageType(StepType.getDefaultInstance())
                                   .build())
            .expansionHandler(Dummy1.class)
            .build();
    jsonExpansionHandlers.add(defExpansionHandlerInfo);

    PmsSdkConfiguration sdkConfiguration =
        PmsSdkConfiguration.builder().moduleType(ModuleType.CD).jsonExpansionHandlers(jsonExpansionHandlers).build();
    List<JsonExpansionInfo> jsonExpansionInfo = PmsSdkInitHelper.getJsonExpansionInfo(sdkConfiguration);
    assertThat(jsonExpansionInfo).hasSize(3);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testGetSupportedSdkFunctorsList() {
    PmsSdkConfiguration sdkConfigurationWithNull = PmsSdkConfiguration.builder().moduleType(ModuleType.CD).build();
    assertThat(PmsSdkInitHelper.getSupportedSdkFunctorsList(sdkConfigurationWithNull)).isEmpty();
    PmsSdkConfiguration sdkConfigurationWithEmpty = PmsSdkConfiguration.builder().moduleType(ModuleType.CD).build();
    assertThat(PmsSdkInitHelper.getSupportedSdkFunctorsList(sdkConfigurationWithEmpty)).isEmpty();

    Map<String, Class<? extends SdkFunctor>> functors = new HashMap<>();
    functors.put("f1", Dummy2.class);
    functors.put("f2", Dummy2.class);
    functors.put("f3", Dummy2.class);
    functors.put("f4", Dummy2.class);

    PmsSdkConfiguration sdkConfiguration =
        PmsSdkConfiguration.builder().moduleType(ModuleType.CD).sdkFunctors(functors).build();
    List<String> expandableFields = PmsSdkInitHelper.getSupportedSdkFunctorsList(sdkConfiguration);
    assertThat(expandableFields).hasSize(4);
    assertThat(expandableFields).contains("f1", "f2", "f3", "f4");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidateSupportedTypesWhenMissingFilterCreators() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("water", "fire"));
    planCreators.add(createPlanCreator("earth"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("fire", "wind", "void"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("water", "earth"));
    variableCreators.add(createVariableCreator("fire"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Map<String, Types> result = PmsSdkInitHelper.calculateSupportedTypes(pipelineServiceInfoProvider);
    assertThat(result).isNotEmpty();
    assertThat(result).containsOnlyKeys("fire");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidateSupportedTypesWhenMorePlanCreatorsThanFilterCreators() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("water", "fire"));
    planCreators.add(createPlanCreator("earth"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("fire"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("water", "earth"));
    variableCreators.add(createVariableCreator("fire"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Map<String, Types> result = PmsSdkInitHelper.calculateSupportedTypes(pipelineServiceInfoProvider);
    assertThat(result).isNotEmpty();
    assertThat(result).containsOnlyKeys("fire");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidateSupportedTypesWhenMissingVariableCreators() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("water", "fire"));
    planCreators.add(createPlanCreator("earth"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("water", "fire", "earth"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("water", "wind"));
    variableCreators.add(createVariableCreator("fire"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Map<String, Types> result = PmsSdkInitHelper.calculateSupportedTypes(pipelineServiceInfoProvider);
    assertThat(result).isNotEmpty();
    assertThat(result).containsOnlyKeys("fire", "water");
  }

  @Test
  @Owner(developers = FERNANDOD)
  @Category(UnitTests.class)
  public void shouldValidateSupportedTypes() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(createPlanCreator("water", "fire"));
    planCreators.add(createPlanCreator("earth"));

    List<FilterJsonCreator> filterCreators = new ArrayList<>();
    filterCreators.add(createFilterCreator("water", "fire", "earth"));

    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(createVariableCreator("water", "earth"));
    variableCreators.add(createVariableCreator("fire"));

    PipelineServiceInfoProvider pipelineServiceInfoProvider =
        createPipelineServiceInfoProvider(planCreators, filterCreators, variableCreators);

    final Map<String, Types> result = PmsSdkInitHelper.calculateSupportedTypes(pipelineServiceInfoProvider);
    assertThat(result).isNotEmpty();
    assertThat(result).containsOnlyKeys("fire", "water", "earth");
  }

  private PartialPlanCreator createPlanCreator(String... elements) {
    PartialPlanCreator creator = mock(PartialPlanCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(elements));
    return creator;
  }

  private FilterJsonCreator createFilterCreator(String... elements) {
    FilterJsonCreator creator = mock(FilterJsonCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(elements));
    return creator;
  }

  private VariableCreator createVariableCreator(String... elements) {
    VariableCreator creator = mock(VariableCreator.class);
    when(creator.getSupportedTypes()).thenReturn(createSupportedTypes(elements));
    return creator;
  }

  private Map<String, Set<String>> createSupportedTypes(String... elements) {
    Map<String, Set<String>> supportedTypes = new HashMap<>(elements.length);
    Arrays.stream(elements).forEach(e -> supportedTypes.put(e, DEFAULT_EMPTY_TYPE_CONTENT));
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

  private static class Dummy1 implements JsonExpansionHandler {
    @Override
    public ExpansionResponse expand(JsonNode fieldValue, ExpansionRequestMetadata metadata, String fqn) {
      return null;
    }
  }

  private static class Dummy2 implements SdkFunctor {
    @Override
    public Object get(Ambiance ambiance, String... args) {
      return null;
    }
  }
}
