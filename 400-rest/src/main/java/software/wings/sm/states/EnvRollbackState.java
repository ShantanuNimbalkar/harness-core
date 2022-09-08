package software.wings.sm.states;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.beans.ExecutionStatus.SUCCESS;
import static io.harness.beans.FeatureName.RESOLVE_DEPLOYMENT_TAGS_BEFORE_EXECUTION;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static java.util.Arrays.asList;
import static software.wings.api.EnvStateExecutionData.Builder.anEnvStateExecutionData;
import static software.wings.sm.StateType.FORK;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.ExecutionStatus;
import io.harness.beans.FeatureName;
import io.harness.beans.OrchestrationWorkflowType;
import io.harness.beans.RepairActionCode;

import io.harness.beans.SweepingOutputInstance;
import io.harness.beans.WorkflowType;
import io.harness.delegate.beans.DelegateResponseData;
import io.harness.ff.FeatureFlagService;
import io.harness.tasks.ResponseData;
import software.wings.api.ArtifactCollectionExecutionData;
import software.wings.api.EnvStateExecutionData;
import software.wings.api.artifact.ServiceArtifactElement;
import software.wings.api.artifact.ServiceArtifactElements;
import software.wings.api.artifact.ServiceArtifactVariableElement;
import software.wings.api.artifact.ServiceArtifactVariableElements;
import software.wings.api.helm.ServiceHelmElement;
import software.wings.api.helm.ServiceHelmElements;
import software.wings.beans.NameValuePair;
import software.wings.beans.WorkflowExecution;
import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.beans.appmanifest.HelmChart;
import software.wings.beans.artifact.Artifact;
import software.wings.service.impl.WorkflowExecutionUpdate;
import software.wings.service.intfc.ApplicationManifestService;
import software.wings.service.intfc.ArtifactService;
import software.wings.service.intfc.ArtifactStreamServiceBindingService;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.WorkflowExecutionService;
import software.wings.service.intfc.WorkflowService;
import software.wings.service.intfc.sweepingoutput.SweepingOutputService;
import software.wings.sm.ExecutionContext;
import software.wings.sm.ExecutionContextImpl;
import software.wings.sm.ExecutionResponse;
import software.wings.sm.State;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import software.wings.sm.StateExecutionData;
import software.wings.sm.StateExecutionInstance;
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
  @Transient @Inject private SweepingOutputService sweepingOutputService;
  @Transient @Inject private FeatureFlagService featureFlagService;
  @Transient @Inject private ApplicationManifestService applicationManifestService;
  @Transient @Inject private ArtifactStreamServiceBindingService artifactStreamServiceBindingService;
  @Transient @Inject private ArtifactService artifactService;
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
    if (stateExecutionData == null) {
      executionContext.getStateExecutionInstance().getStateExecutionMap().entrySet().stream().filter(entry -> {
        if (entry.getValue().getStateType() == FORK.getType()) {
          ForkState.ForkStateExecutionData forkState = (ForkState.ForkStateExecutionData) entry.getValue();
          forkState.get
          return forkState.getForkStateNames().contains(this.getName().replace(ROLLBACK_PREFIX, ""));
        }
        return false;
      }).map(it -> it.getValue()).findFirst();
    }
    WorkflowExecution workflowExecution = executionService.getWorkflowExecution(executionContext.getAppId(), stateExecutionData.getWorkflowExecutionId());
    WorkflowExecution rollbackExecution = executionService.triggerRollbackExecutionWorkflow(executionContext.getAppId(), workflowExecution, true);

    envStateExecutionData.setWorkflowExecutionId(rollbackExecution.getUuid());
    envStateExecutionData.setOrchestrationWorkflowType(rollbackExecution.getOrchestrationType());
    return ExecutionResponse.builder()
        .async(true)
        .correlationIds(asList(rollbackExecution.getUuid()))
        .stateExecutionData(envStateExecutionData)
        .build();
  }

  @Override
  public ExecutionResponse handleAsyncResponse(ExecutionContext context, Map<String, ResponseData> response) {
    EnvState.EnvExecutionResponseData responseData = (EnvState.EnvExecutionResponseData) response.values().iterator().next();
    ExecutionResponse.ExecutionResponseBuilder executionResponseBuilder =
        ExecutionResponse.builder().executionStatus(responseData.getStatus());

    return executionResponseBuilder.build();
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

  public static class EnvRollbackExecutionResponseData implements DelegateResponseData
  {
    private String workflowExecutionId;
    private ExecutionStatus status;

    /**
     * Instantiates a new Env execution response data.
     *
     * @param workflowExecutionId the workflow execution id
     * @param status              the status
     */
    public EnvRollbackExecutionResponseData(String workflowExecutionId, ExecutionStatus status) {
      this.workflowExecutionId = workflowExecutionId;
      this.status = status;
    }

    /**
     * Gets workflow execution id.
     *
     * @return the workflow execution id
     */
    public String getWorkflowExecutionId() {
      return workflowExecutionId;
    }

    /**
     * Sets workflow execution id.
     *
     * @param workflowExecutionId the workflow execution id
     */
    public void setWorkflowExecutionId(String workflowExecutionId) {
      this.workflowExecutionId = workflowExecutionId;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public ExecutionStatus getStatus() {
      return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(ExecutionStatus status) {
      this.status = status;
    }
  }
}
