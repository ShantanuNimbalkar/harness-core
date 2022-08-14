/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ci.buildstate;

import io.harness.beans.yaml.extended.infrastrucutre.*;
import io.harness.ci.integrationstage.VmInitializeTaskParamsBuilder;
import io.harness.delegate.beans.ci.*;
import io.harness.exception.ngexception.CIStageExecutionException;

import static java.lang.String.format;

public class InfraInfoBuilder {
    public InfraInfo GetInfraInfo(Infrastructure infrastructure, String stageRuntimeId) {
        Infrastructure.Type type = infrastructure.getType();
        switch (type) {
            case VM:
                if (((VmInfraYaml) infrastructure).getSpec() == null) {
                    throw new CIStageExecutionException("VM input infrastructure can not be empty");
                }

                VmInfraYaml vmInfraYaml = (VmInfraYaml) infrastructure;
                if (vmInfraYaml.getSpec().getType() != VmInfraSpec.Type.POOL) {
                    throw new CIStageExecutionException(
                            format("Invalid VM infrastructure spec type: %s", vmInfraYaml.getSpec().getType()));
                }
                VmPoolYaml vmPoolYaml = (VmPoolYaml) vmInfraYaml.getSpec();
                String poolId = VmInitializeTaskParamsBuilder.getPoolName(vmPoolYaml);
                String harnessImageConnectorRef = (vmPoolYaml.getSpec().getHarnessImageConnectorRef().getValue());
                return VmInfraInfo.builder().poolId(poolId).harnessImageConnectorRef(harnessImageConnectorRef).build();
            case DOCKER:
                if (((DockerInfraYaml) infrastructure).getSpec() == null) {
                    throw new CIStageExecutionException("Docker input infrastructure can not be empty");
                }
                return DockerInfraInfo.builder().stageRuntimeId(stageRuntimeId).build();
            case HOSTED_VM:
                return HostedVmInfraInfo.builder().stageRuntimeId(stageRuntimeId).build();
            case KUBERNETES_HOSTED:
                return K8InfraInfo.builder().stageRuntimeId(stageRuntimeId).build();
            default:
                throw new CIStageExecutionException(String.format("InfraInfo is not supported for %s", type.toString()));
        }
    }
}
