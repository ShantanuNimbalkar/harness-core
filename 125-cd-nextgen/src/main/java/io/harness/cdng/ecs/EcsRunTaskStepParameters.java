package io.harness.cdng.ecs;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.manifest.yaml.ManifestConfig;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfig;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfigWrapper;
import io.harness.ecs.EcsCommandUnitConstants;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.pms.yaml.ParameterField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Arrays;
import java.util.List;

@OwnedBy(HarnessTeam.CDP)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias("ecsRunTaskStepParameters")
@RecasterAlias("io.harness.cdng.ecs.EcsRunTaskStepParameters")
public class EcsRunTaskStepParameters extends EcsRunTaskBaseStepInfo implements EcsSpecParameters {
  @Builder(builderMethodName = "infoBuilder")
  public EcsRunTaskStepParameters(
      ParameterField<List<TaskSelectorYaml>> delegateSelectors,
      ParameterField<StoreConfigWrapper> taskDefinition,
      ParameterField<Boolean> waitForSteadyState) {
    super(delegateSelectors, taskDefinition, waitForSteadyState);
  }

  public List<String> getCommandUnits() {
    return Arrays.asList(EcsCommandUnitConstants.runTask.toString());
  }
}
