package io.harness.cdng.ecs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.SwaggerConstants;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfigWrapper;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.pms.yaml.ParameterField;
import io.harness.pms.yaml.SkipAutoEvaluation;
import io.harness.yaml.YamlSchemaTypes;
import io.harness.yaml.schema.beans.SupportedPossibleFieldTypes;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.Wither;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.runtime;

@OwnedBy(HarnessTeam.CDP)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("ecsRunTaskBaseStepInfo")
@FieldNameConstants(innerTypeName = "EcsRunTaskBaseStepInfoKeys")
public class EcsRunTaskBaseStepInfo {
  @YamlSchemaTypes({runtime})
  @ApiModelProperty(dataType = SwaggerConstants.STRING_LIST_CLASSPATH)
  ParameterField<List<TaskSelectorYaml>> delegateSelectors;

  @Wither
  @JsonProperty("taskDefinition")
  @ApiModelProperty(dataType = "io.harness.cdng.manifest.yaml.storeConfig.StoreConfigWrapper")
  @SkipAutoEvaluation
  ParameterField<StoreConfigWrapper> taskDefinition;

  @YamlSchemaTypes(value = {SupportedPossibleFieldTypes.runtime})
  @ApiModelProperty(dataType = SwaggerConstants.BOOLEAN_CLASSPATH)
  private ParameterField<Boolean> waitForSteadyState;

}
