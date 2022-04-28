package software.wings.service.impl;

import static io.harness.annotations.dev.HarnessModule._950_NG_AUTHENTICATION_SERVICE;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.encryption.EncryptionReflectUtils.getEncryptedRefField;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.exception.InvalidRequestException;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.annotation.EncryptableSetting;
import software.wings.beans.sso.LdapConnectionSettings;
import software.wings.helpers.ext.ldap.LdapConstants;
import software.wings.service.intfc.security.EncryptionService;
import software.wings.service.intfc.security.SecretManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@TargetModule(_950_NG_AUTHENTICATION_SERVICE)
@OwnedBy(HarnessTeam.PL)
public class SSOServiceHelper {
  @Inject private EncryptionService encryptionService;

  public void encryptLdapSecret(LdapConnectionSettings connectionSettings, SecretManager secretManager) {
    String existingPasswordType = "";
    existingPasswordType = connectionSettings.getPasswordType();
    if (isNotEmpty(connectionSettings.getBindSecret())) {
      if ((!connectionSettings.getBindPassword().isEmpty())
          && (!connectionSettings.getBindPassword().equals(LdapConstants.MASKED_STRING))) {
        throw new InvalidRequestException("Either Enter password or select a secret");
      }
      connectionSettings.setPasswordType(LdapConnectionSettings.SECRET);
      connectionSettings.setEncryptedBindSecret(String.valueOf(connectionSettings.getBindSecret()));
      connectionSettings.setBindSecret(null);
      List<EncryptedDataDetail> encryptionDetails =
          secretManager.getEncryptionDetails((EncryptableSetting) connectionSettings, null, null);
      encryptionService.decrypt(connectionSettings, encryptionDetails, false);
      if (existingPasswordType != null) {
        if (existingPasswordType.equals(LdapConnectionSettings.INLINE_SECRET)) {
          // If the LDAP Setting was already used with Inline Password, and now when they have choosed Secret, hence
          // deleting the orphan secret
          String oldEncryptedBindPassword = connectionSettings.getEncryptedBindPassword();
          if (isNotEmpty(oldEncryptedBindPassword)) {
            secretManager.deleteSecret(
                connectionSettings.getAccountId(), oldEncryptedBindPassword, new HashMap<>(), false);
          }
        }
      }
    }
  }

  /**
   * copyToEncryptedRefFields copies the value of encrypted fields to encrypted ref fields. This method is needed
   * because UI passes the ID in the encrypted field and not the ref field.
   *
   * It does the following type of conversion:
   * { "password": "val1", "encryptedPassword": "..." }
   * ->
   * { "password": null, "encryptedPassword": "val1" }
   *
   * @param object the encryptable setting to mutate
   */
  public void copyToEncryptedRefFields(EncryptableSetting object) {
    List<Field> encryptedFields = object.getEncryptedFields();
    try {
      for (Field f : encryptedFields) {
        f.setAccessible(true);
        char[] fieldValue = (char[]) f.get(object);
        if (fieldValue == null) {
          // Ignore if encrypted field value is null. This is required for yaml.
          continue;
        }
        Field encryptedRefField = getEncryptedRefField(f, object);
        encryptedRefField.setAccessible(true);
        encryptedRefField.set(object, String.valueOf(fieldValue));
        f.set(object, null);
      }
    } catch (Exception e) {
      throw new InvalidRequestException("Error in secretId to Ref Conversion", e);
    }
  }
}
