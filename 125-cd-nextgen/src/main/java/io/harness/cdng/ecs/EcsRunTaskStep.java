package io.harness.cdng.ecs;

import com.google.inject.Inject;
import io.harness.account.services.AccountService;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.CDStepHelper;
import io.harness.cdng.ecs.beans.EcsCanaryDeleteDataOutcome;
import io.harness.cdng.ecs.beans.EcsCanaryDeleteOutcome;
import io.harness.cdng.ecs.beans.EcsExecutionPassThroughData;
import io.harness.cdng.infra.beans.InfrastructureOutcome;
import io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants;
import io.harness.data.structure.EmptyPredicate;
import io.harness.delegate.beans.ecs.EcsCanaryDeleteResult;
import io.harness.delegate.beans.logstreaming.CommandUnitsProgress;
import io.harness.delegate.task.ecs.EcsCommandTypeNG;
import io.harness.delegate.task.ecs.request.EcsCanaryDeleteRequest;
import io.harness.delegate.task.ecs.response.EcsCanaryDeleteResponse;
import io.harness.delegate.task.ecs.response.EcsCommandResponse;
import io.harness.exception.ExceptionUtils;
import io.harness.exception.InvalidRequestException;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.logging.CommandExecutionStatus;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.plancreator.steps.common.rollback.TaskExecutableWithRollbackAndRbac;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.failure.FailureInfo;
import io.harness.pms.contracts.execution.tasks.SkipTaskRequest;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.data.OptionalSweepingOutput;
import io.harness.pms.sdk.core.plan.creation.yaml.StepOutcomeGroup;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outcome.OutcomeService;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.steps.StepHelper;
import io.harness.steps.StepUtils;
import io.harness.supplier.ThrowingSupplier;
import lombok.extern.slf4j.Slf4j;

import static io.harness.exception.WingsException.USER;

@OwnedBy(HarnessTeam.CDP)
@Slf4j
public class EcsRunTaskStep extends TaskExecutableWithRollbackAndRbac<EcsCommandResponse> {
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(ExecutionNodeType.ECS_RUN_TASK.getYamlType())
                                               .setStepCategory(StepCategory.STEP)
                                               .build();
  public static final String ECS_RUN_TASK_COMMAND_NAME = "EcsRunTask";

  @Inject private ExecutionSweepingOutputService executionSweepingOutputService;
  @Inject private OutcomeService outcomeService;
  @Inject private EcsStepCommonHelper ecsStepCommonHelper;
  @Inject private AccountService accountService;
  @Inject private StepHelper stepHelper;

  @Override
  public void validateResources(Ambiance ambiance, StepElementParameters stepParameters) {
    // Nothing to validate
  }

  @Override
  public StepResponse handleTaskResultWithSecurityContext(Ambiance ambiance,
      StepElementParameters stepElementParameters, ThrowingSupplier<EcsCommandResponse> responseDataSupplier)
      throws Exception {
    return null;
  }


  @Override
  public TaskRequest obtainTaskAfterRbac(
      Ambiance ambiance, StepElementParameters stepElementParameters, StepInputPackage inputPackage) {
    final String accountId = AmbianceUtils.getAccountId(ambiance);

    EcsRunTaskStepParameters ecsRunTaskStepParameters = null;
    return null;
  }

  @Override
  public Class<StepElementParameters> getStepParametersClass() {
    return StepElementParameters.class;
  }
}
