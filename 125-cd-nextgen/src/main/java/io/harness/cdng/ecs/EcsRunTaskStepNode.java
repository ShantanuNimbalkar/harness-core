package io.harness.cdng.ecs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.pipeline.CdAbstractStepNode;
import io.harness.executions.steps.StepSpecTypeConstants;
import io.harness.yaml.core.StepSpecType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@OwnedBy(HarnessTeam.CDP)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(StepSpecTypeConstants.ECS_RUN_TASK)
@TypeAlias("EcsRunTaskStepNode")
@RecasterAlias("io.harness.cdng.ecs.EcsRunTaskStepNode")
public class EcsRunTaskStepNode extends CdAbstractStepNode {
  @JsonProperty("type")
  @NotNull
  EcsRunTaskStepNode.StepType type = StepType.EcsRunTask;
  @JsonProperty("spec")
  @JsonTypeInfo(use = NAME, property = "type", include = EXTERNAL_PROPERTY, visible = true)
  EcsRunTaskStepInfo ecsRunTaskStepInfo;

  @Override
  public String getType() {
    return StepSpecTypeConstants.ECS_RUN_TASK;
  }

  @Override
  public StepSpecType getStepSpecType() {
    return ecsRunTaskStepInfo;
  }

  enum StepType {
    EcsRunTask(StepSpecTypeConstants.ECS_RUN_TASK);
    @Getter String name;
    StepType(String name) {
      this.name = name;
    }
  }
}
