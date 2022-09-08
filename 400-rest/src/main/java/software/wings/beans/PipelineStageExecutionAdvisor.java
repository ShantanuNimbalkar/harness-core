/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.beans.ExecutionInterruptType.MARK_FAILED;
import static io.harness.beans.ExecutionInterruptType.PAUSE_FOR_INPUTS;
import static io.harness.beans.ExecutionInterruptType.ROLLBACK_PREVIOUS_STAGES_ON_PIPELINE;
import static io.harness.beans.ExecutionStatus.FAILED;
import static io.harness.beans.ExecutionStatus.PAUSED;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import static software.wings.api.EnvStateExecutionData.Builder.anEnvStateExecutionData;
import static software.wings.sm.ExecutionEventAdvice.ExecutionEventAdviceBuilder.anExecutionEventAdvice;
import static software.wings.sm.StateType.ENV_LOOP_STATE;
import static software.wings.sm.StateType.ENV_STATE;
import static software.wings.sm.StateType.FORK;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.ExecutionStatus;
import io.harness.context.ContextElementType;

import software.wings.api.EnvStateExecutionData;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.WorkflowExecutionService;
import software.wings.service.intfc.WorkflowService;
import software.wings.sm.ExecutionContextImpl;
import software.wings.sm.ExecutionEvent;
import software.wings.sm.ExecutionEventAdvice;
import software.wings.sm.ExecutionEventAdvisor;
import software.wings.sm.ExecutionResponse;
import software.wings.sm.State;
import software.wings.sm.StateExecutionInstance;
import software.wings.sm.StateMachine;
import software.wings.sm.Transition;
import software.wings.sm.WorkflowStandardParams;
import software.wings.sm.states.ForkState;
import software.wings.sm.states.WorkflowState;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(CDC)
@Slf4j
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
public class PipelineStageExecutionAdvisor implements ExecutionEventAdvisor {
  @Inject private transient WorkflowExecutionService workflowExecutionService;
  @Inject private transient WorkflowService workflowService;
  @Inject private transient PipelineService pipelineService;

  public static final String ROLLBACK_PREFIX = "Rollback ";

  @Override
  public ExecutionEventAdvice onExecutionEvent(ExecutionEvent executionEvent) {
    State state = executionEvent.getState();
    ExecutionContextImpl context = executionEvent.getContext();
    WorkflowExecution workflowExecution =
        workflowExecutionService.getWorkflowExecution(context.getAppId(), context.getWorkflowExecutionId());
    StateExecutionInstance stateExecutionInstance = context.getStateExecutionInstance();

    if (!ENV_STATE.name().equals(state.getStateType()) && !ENV_LOOP_STATE.name().equals(state.getStateType())) {
      return null;
    }

    WorkflowState workflowState = (WorkflowState) state;
    if (stateExecutionInstance.isContinued()) {
      return null;
    }
    ExecutionResponse executionResponse = workflowState.checkDisableAssertion(context, workflowService, log);
    if (executionResponse != null) {
      return anExecutionEventAdvice()
          .withExecutionInterruptType(executionResponse.getExecutionStatus() == FAILED ? MARK_FAILED : null)
          .withExecutionResponse(executionResponse)
          .build();
    }
    // todo: add feature flag
    if (shouldRollback(
            context.getAppId(), workflowExecution.getPipelineSummary().getPipelineId(), stateExecutionInstance)) {
      StateMachine sm = context.getStateMachine();
      State prevState = sm.getStates().get(sm.getStates().indexOf(state) - 1);
      List<Transition> transitions =
          sm.getTransitions()
              .stream()
              .filter(transition -> transition.getFromState().getName().equals(ROLLBACK_PREFIX + prevState.getName()))
              .collect(Collectors.toList());
      State rollbackState = prevState;
      if (transitions.isEmpty()) {
        Transition rollbackTransition = sm.getTransitions()
                                            .stream()
                                            .filter(transition
                                                -> transition.getFromState().getName().startsWith("rollback-")
                                                    && FORK.getType().equals(transition.getFromState().getStateType())
                                                    && ((ForkState) transition.getFromState())
                                                           .getForkStateNames()
                                                           .contains(ROLLBACK_PREFIX + prevState.getName()))
                                            .findAny()
                                            .get();
        rollbackState = rollbackTransition.getFromState();
      }
      return anExecutionEventAdvice()
          .withExecutionInterruptType(ROLLBACK_PREVIOUS_STAGES_ON_PIPELINE)
          .withNextStateName(rollbackState.getStateType().equals(FORK.getType()) ? rollbackState.getName() : ROLLBACK_PREFIX + rollbackState.getName())
          .build();
    }

    WorkflowStandardParams workflowStandardParams = context.getContextElement(ContextElementType.STANDARD);
    if (workflowStandardParams != null && workflowStandardParams.isContinueWithDefaultValues()) {
      log.info(String.format(
          "Continue With defaults option is selection for execution: %s. Hence not pausing the stage for inputs",
          workflowExecution.getUuid()));
      return null;
    }

    List<String> runtimeInputsVariables = workflowState.getRuntimeInputVariables();
    if (isNotEmpty(runtimeInputsVariables)) {
      EnvStateExecutionData envStateExecutionData =
          anEnvStateExecutionData().withWorkflowId(workflowState.getWorkflowId()).build();
      executionResponse =
          ExecutionResponse.builder().executionStatus(PAUSED).stateExecutionData(envStateExecutionData).build();
      return anExecutionEventAdvice()
          .withExecutionInterruptType(PAUSE_FOR_INPUTS)
          .withExecutionResponse(executionResponse)
          .withTimeout(workflowState.getTimeout())
          .withActionOnTimeout(workflowState.getTimeoutAction())
          .withUserGroupIdsToNotify(workflowState.getUserGroupIds())
          .withNextChildStateMachineId(stateExecutionInstance.getChildStateMachineId())
          .build();
    }

    return null;
  }

  private boolean shouldRollback(String appId, String pipelineId, StateExecutionInstance instance) {
    Pipeline pipeline = pipelineService.getPipeline(appId, pipelineId);
    if (pipeline.rollbackPreviousStages && FAILED.equals(instance.getStatus())) {
      return true;
    }
    return false;
  }
}
