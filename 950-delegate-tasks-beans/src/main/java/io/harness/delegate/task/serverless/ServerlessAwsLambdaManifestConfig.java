/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.serverless;

import io.harness.delegate.beans.storeconfig.GitStoreDelegateConfig;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServerlessAwsLambdaManifestConfig implements ServerlessManifestConfig {
  String manifestPath;
  String configOverridePath;
  GitStoreDelegateConfig gitStoreDelegateConfig;

  @Override
  public ServerlessManifestType getServerlessManifestType() {
    return ServerlessManifestType.SERVERLESS_AWS_LAMBDA_MANIFEST;
  }
}
