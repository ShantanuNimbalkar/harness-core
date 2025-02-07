/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.core.encryptors;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegatetasks.ValidateCustomSecretManagerSecretReferenceTaskParameters;
import io.harness.delegatetasks.ValidateSecretManagerConfigurationTaskParameters;
import io.harness.encryptors.CustomEncryptor;
import io.harness.security.encryption.EncryptedDataParams;
import io.harness.security.encryption.EncryptedRecord;
import io.harness.security.encryption.EncryptedRecordData;
import io.harness.security.encryption.EncryptionConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import javax.validation.executable.ValidateOnExecution;

@ValidateOnExecution
@OwnedBy(PL)
@Singleton
public class NGManagerCustomEncryptor implements CustomEncryptor {
  private final NGManagerEncryptorHelper ngManagerEncryptorHelper;
  private static final String SCRIPT = "Script";
  private static final String EXPRESSION_FUNCTOR_TOKEN = "expressionFunctorToken";

  @Inject
  public NGManagerCustomEncryptor(NGManagerEncryptorHelper ngManagerEncryptorHelper) {
    this.ngManagerEncryptorHelper = ngManagerEncryptorHelper;
  }

  @Override
  public boolean validateReference(
      String accountId, Set<EncryptedDataParams> params, EncryptionConfig encryptionConfig) {
    String script = getParameter(SCRIPT, params);
    return validateReference(accountId, script, params, encryptionConfig);
  }

  public boolean validateReference(
      String accountId, String script, Set<EncryptedDataParams> params, EncryptionConfig encryptionConfig) {
    ValidateCustomSecretManagerSecretReferenceTaskParameters parameters =
        ValidateCustomSecretManagerSecretReferenceTaskParameters.builder()
            .encryptedRecord(EncryptedRecordData.builder().parameters(params).build())
            .encryptionConfig(encryptionConfig)
            .script(script)
            .build();
    int expressionFunctorToken = Integer.parseInt(getParameter(EXPRESSION_FUNCTOR_TOKEN, params));
    return ngManagerEncryptorHelper.validateCustomSecretManagerSecretReference(
        accountId, expressionFunctorToken, parameters);
  }

  @Override
  public char[] fetchSecretValue(String accountId, EncryptedRecord encryptedRecord, EncryptionConfig encryptionConfig) {
    String script = getParameter(SCRIPT, encryptedRecord);
    int expressionFunctorToken = Integer.parseInt(getParameter(EXPRESSION_FUNCTOR_TOKEN, encryptedRecord));
    return ngManagerEncryptorHelper.fetchSecretValue(
        accountId, script, expressionFunctorToken, encryptedRecord, encryptionConfig);
  }

  @Override
  public boolean validateCustomConfiguration(String accountId, EncryptionConfig encryptionConfig) {
    ValidateSecretManagerConfigurationTaskParameters parameters =
        ValidateSecretManagerConfigurationTaskParameters.builder().encryptionConfig(encryptionConfig).build();
    return ngManagerEncryptorHelper.validateConfiguration(accountId, parameters);
  }

  @Override
  public String resolveSecretManagerConfig(
      String accountId, Set<EncryptedDataParams> params, EncryptionConfig encryptionConfig) {
    String script = getParameter(SCRIPT, params);
    ValidateCustomSecretManagerSecretReferenceTaskParameters parameters =
        ValidateCustomSecretManagerSecretReferenceTaskParameters.builder()
            .encryptedRecord(EncryptedRecordData.builder().parameters(params).build())
            .encryptionConfig(encryptionConfig)
            .script(script)
            .build();
    int expressionFunctorToken = Integer.parseInt(getParameter(EXPRESSION_FUNCTOR_TOKEN, params));
    return ngManagerEncryptorHelper.resolveSecretManagerConfig(accountId, expressionFunctorToken, parameters);
  }

  public String getParameter(String parameterName, EncryptedRecord encryptedRecord) {
    return getParameter(parameterName, encryptedRecord.getParameters());
  }

  public String getParameter(String parameterName, Set<EncryptedDataParams> encryptedDataParamsSet) {
    if (encryptedDataParamsSet == null) {
      return null;
    }
    Optional<EncryptedDataParams> parameter =
        encryptedDataParamsSet.stream().filter(x -> x.getName().equals(parameterName)).findFirst();
    if (parameter.isPresent()) {
      return parameter.get().getValue();
    }
    return null;
  }
}