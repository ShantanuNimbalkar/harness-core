package software.wings.sm.states;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static java.util.Arrays.asList;
import static software.wings.api.EnvStateExecutionData.Builder.anEnvStateExecutionData;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.RepairActionCode;

import software.wings.api.EnvStateExecutionData;
import software.wings.beans.WorkflowExecution;
import software.wings.service.impl.WorkflowExecutionUpdate;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.WorkflowExecutionService;
import software.wings.service.intfc.WorkflowService;
import software.wings.sm.ExecutionContext;
import software.wings.sm.ExecutionContextImpl;
import software.wings.sm.ExecutionResponse;
import software.wings.sm.State;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import software.wings.sm.StateExecutionData;
import software.wings.sm.StateMachine;
import software.wings.sm.StateType;
import software.wings.sm.rollback.RollbackStateMachineGenerator;

@OwnedBy(CDC)
@Attributes(title = "EnvRollback")
@Slf4j
@FieldNameConstants(innerTypeName = "EnvRollbackStateKeys")
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
public class EnvRollbackState extends State implements WorkflowState {
  @Setter @SchemaIgnore private String pipelineId;
  @Setter @SchemaIgnore private String pipelineStageElementId;
  @Setter @SchemaIgnore private int pipelineStageParallelIndex;
  @Setter @SchemaIgnore private String stageName;

  @JsonIgnore @SchemaIgnore private Map<String, String> workflowVariables;

  @Setter @SchemaIgnore List<String> runtimeInputVariables;
  @Setter @SchemaIgnore long timeout;
  @Setter @SchemaIgnore List<String> userGroupIds;
  @Setter @SchemaIgnore RepairActionCode timeoutAction;

  @Inject private Injector injector;
  @Transient @Inject private WorkflowService workflowService;
  @Transient @Inject private PipelineService pipelineService;
  @Transient @Inject private WorkflowExecutionService executionService;
  @Transient @Inject private WorkflowExecutionUpdate executionUpdate;
  @Inject private RollbackStateMachineGenerator rollbackStateMachineGenerator;

  private static String ROLLBACK_PREFIX = "Rollback ";

  public EnvRollbackState(String name) {
    super(name, StateType.ENV_ROLLBACK_STATE.name());
  }

  @Override
  public ExecutionResponse execute(ExecutionContext context) {
    ExecutionContextImpl executionContext = (ExecutionContextImpl) context;
    String workflowId = context.getWorkflowId();

    EnvStateExecutionData envStateExecutionData = anEnvStateExecutionData().withWorkflowId(workflowId).build();

    EnvStateExecutionData stateExecutionData = (EnvStateExecutionData) executionContext.getStateExecutionInstance().getStateExecutionMap().get(this.getName().replace(ROLLBACK_PREFIX, ""));
    WorkflowExecution workflowExecution = executionService.getWorkflowExecution(executionContext.getAppId(), stateExecutionData.getWorkflowExecutionId());
    WorkflowExecution rollbackExecution = executionService.triggerRollbackExecutionWorkflow(executionContext.getAppId(), workflowExecution);

    envStateExecutionData.setWorkflowExecutionId(rollbackExecution.getUuid());
    return ExecutionResponse.builder()
        .async(true)
        .correlationIds(asList(rollbackExecution.getUuid()))
        .stateExecutionData(envStateExecutionData)
        .build();
  }

  @Override
  public void handleAbortEvent(ExecutionContext context) {}

  @Override
  public List<String> getRuntimeInputVariables() {
    return null;
  }

  @Override
  public long getTimeout() {
    return 0;
  }

  @Override
  public List<String> getUserGroupIds() {
    return null;
  }

  @Override
  public RepairActionCode getTimeoutAction() {
    return null;
  }

  @Override
  public Map<String, String> getWorkflowVariables() {
    return null;
  }

  @Override
  public boolean isContinued() {
    return false;
  }

  @Override
  public void setContinued(boolean continued) {}

  @Override
  public String getPipelineStageElementId() {
    return null;
  }

  @Override
  public int getPipelineStageParallelIndex() {
    return 0;
  }

  @Override
  public String getStageName() {
    return null;
  }

  @Override
  public String getDisableAssertion() {
    return null;
  }

  @Override
  public String getWorkflowId() {
    return null;
  }

  @Override
  public ExecutionResponse checkDisableAssertion(
      ExecutionContextImpl context, WorkflowService workflowService, Logger log) {
    return WorkflowState.super.checkDisableAssertion(context, workflowService, log);
  }
}
