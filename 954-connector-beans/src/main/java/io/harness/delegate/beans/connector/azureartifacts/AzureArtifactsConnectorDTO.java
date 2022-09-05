/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.beans.connector.azureartifacts;

import io.harness.beans.DecryptableEntity;
import io.harness.connector.DelegateSelectable;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;

import java.util.List;
import java.util.Set;

public class AzureArtifactsConnectorDTO extends ConnectorConfigDTO implements DelegateSelectable {
  @Override
  public List<DecryptableEntity> getDecryptableEntities() {
    return null;
  }

  @Override
  public Set<String> getDelegateSelectors() {
    return null;
  }

  @Override
  public void setDelegateSelectors(Set<String> delegateSelectors) {}
}
