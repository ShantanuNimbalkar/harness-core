/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.customDeployment;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.CDStepHelper;
import io.harness.data.structure.CollectionUtils;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.task.shell.ShellScriptTaskNG;
import io.harness.delegate.task.shell.ShellScriptTaskResponseNG;
import io.harness.exception.ExceptionUtils;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.plancreator.steps.common.rollback.TaskExecutableWithRollbackAndRbac;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.StepHelper;
import io.harness.steps.StepUtils;
import io.harness.supplier.ThrowingSupplier;

import software.wings.beans.TaskType;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CDP)
@Slf4j
public class FetchInstanceScriptStep extends TaskExecutableWithRollbackAndRbac<ShellScriptTaskResponseNG> {
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(ExecutionNodeType.AZURE_TRAFFIC_SHIFT.getYamlType())
                                               .setStepCategory(StepCategory.STEP)
                                               .build();
  @Inject private CDStepHelper cdStepHelper;
  @Inject private KryoSerializer kryoSerializer;
  @Inject private StepHelper stepHelper;

  @Override
  public void validateResources(Ambiance ambiance, StepElementParameters stepParameters) {
    // Azure Connector is validated in InfrastructureStep
  }

  @Override
  public TaskRequest obtainTaskAfterRbac(
      Ambiance ambiance, StepElementParameters stepParameters, StepInputPackage inputPackage) {
    FetchInstanceScriptStepParameters stepSpec = (FetchInstanceScriptStepParameters) stepParameters.getSpec();
    List<TaskSelectorYaml> taskSelectors = stepSpec.getDelegateSelectors().getValue();

    final TaskData taskData = TaskData.builder()
                                  .async(true)
                                  .timeout(CDStepHelper.getTimeoutInMillis(stepParameters))
                                  .taskType(TaskType.SHELL_SCRIPT_TASK_NG.name())
                                  .parameters(new Object[] {})
                                  .build();
    return StepUtils.prepareCDTaskRequest(ambiance, taskData, kryoSerializer,
        CollectionUtils.emptyIfNull(StepUtils.generateLogKeys(
            StepUtils.generateLogAbstractions(ambiance), Collections.singletonList(ShellScriptTaskNG.COMMAND_UNIT))),
        null, null, TaskSelectorYaml.toTaskSelector(stepSpec.getDelegateSelectors()),
        stepHelper.getEnvironmentType(ambiance));
  }

  @Override
  public StepResponse handleTaskResultWithSecurityContext(Ambiance ambiance, StepElementParameters stepParameters,
      ThrowingSupplier<ShellScriptTaskResponseNG> responseDataSupplier) throws Exception {
    StepResponseBuilder builder = StepResponse.builder();

    ShellScriptTaskResponseNG response;
    try {
      response = responseDataSupplier.get();
    } catch (Exception ex) {
      log.error("Error while processing Fetch Instance script task response: {}", ExceptionUtils.getMessage(ex), ex);
      throw ex;
    }
    builder.unitProgressList(response.getUnitProgressData().getUnitProgresses());
    builder.status(Status.SUCCEEDED);
    return builder.build();
  }

  @Override
  public Class<StepElementParameters> getStepParametersClass() {
    return StepElementParameters.class;
  }
}
