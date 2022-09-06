package io.harness.migrations.all;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import io.harness.beans.EncryptedData;
import io.harness.k8s.model.KubernetesClusterAuthType;
import io.harness.migrations.Migration;
import io.harness.mongo.MongoPersistence;
import io.harness.persistence.HIterator;

import software.wings.beans.KubernetesClusterConfig;
import software.wings.beans.SettingAttribute;
import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SettingAttributesServiceAccountTokenMigration implements Migration {
  @Inject private WingsPersistence wingsPersistence;
  @Inject private MongoPersistence mongoPersistence;
  @Override
  public void migrate() {
    log.info("Starting Cloud Provider migration");
    String accountId =
        "yTbabkw4SdCPZdnEWynUNg"; // Account Id for the customer- "Kroger" for whom the migration is required
    Set<String> tokenIds = new HashSet<>();

    try(HIterator<SettingAttribute> settingAttributes = new HIterator<>(
        wingsPersistence.createQuery(SettingAttribute.class)
            .filter(SettingAttribute.SettingAttributeKeys.accountId, accountId)
            .filter(SettingAttribute.SettingAttributeKeys.category, SettingAttribute.SettingCategory.CLOUD_PROVIDER)
            .filter(SettingAttribute.SettingAttributeKeys.value_type, "KUBERNETES_CLUSTER")
            .fetch());
    HIterator<EncryptedData> encryptedRecords =
        new HIterator<>(wingsPersistence.createQuery(EncryptedData.class)
                            .filter(EncryptedData.EncryptedDataKeys.accountId, accountId)
                            .fetch());
    ) {
      Map<String, String> tokenIdsMap = new HashMap<>();
      while (encryptedRecords.hasNext()) {
        EncryptedData encryptedData = encryptedRecords.next();
        tokenIdsMap.put(encryptedData.getName(), encryptedData.getUuid());
        tokenIds.add(encryptedData.getUuid());
      }
      while (settingAttributes.hasNext()) {
        SettingAttribute settingAttribute = settingAttributes.next();
        if (((KubernetesClusterConfig) settingAttribute.getValue()).getAuthType()
                != KubernetesClusterAuthType.SERVICE_ACCOUNT) {
          continue;
        }
        if (tokenIds.contains(
                ((KubernetesClusterConfig) settingAttribute.getValue()).getEncryptedServiceAccountToken())) {
          continue;
        }
        UpdateOperations<SettingAttribute> ops = mongoPersistence.createUpdateOperations(SettingAttribute.class);
        String currentToken = tokenIdsMap.get(((KubernetesClusterConfig) settingAttribute.getValue()).getEncryptedServiceAccountToken());
        if (isNotEmpty(currentToken)) {
          ops.set("value.encryptedServiceAccountToken", currentToken);
          mongoPersistence.update(settingAttribute, ops);
        }
      }
    }
  }
}
