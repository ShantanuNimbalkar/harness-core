package io.harness.cvng.utils;

import io.harness.delegate.beans.connector.awsconnector.AwsConnectorDTO;
import io.harness.delegate.beans.connector.awsconnector.AwsCredentialType;
import io.harness.delegate.beans.connector.awsconnector.AwsManualConfigSpecDTO;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

public class CloudWatchUtils {
  public static String getBaseUrl(String region, String serviceName) {
    return "https://" + serviceName + "." + region + "amazon.aws.com";
  }

  public static List<Map<String, Object>> getRequestPayload(String query, String metricName, String metricIdentifier) {
    List<Map<String, Object>> metricQueries = new ArrayList<>();
    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put("Expression", query);
    queryMap.put("Label", metricName);
    queryMap.put("Id", metricIdentifier);
    queryMap.put("Period", 60);
    metricQueries.add(queryMap);
    return metricQueries;
  }

  public static Map<String, Object> getDslEnvVariables(String region, String group, String query, String metricName,
      String metricIdentifier, String service, AwsConnectorDTO connectorDTO) {
    AWSCredentials awsCredentials = getAwsCredentials(connectorDTO);
    Map<String, Object> dslEnvVariables = new HashMap<>();
    dslEnvVariables.put("region", region);
    dslEnvVariables.put("groupName", group);
    dslEnvVariables.put("awsSecretKey", awsCredentials.getAWSSecretKey());
    dslEnvVariables.put("awsAccessKey", awsCredentials.getAWSAccessKeyId());
    dslEnvVariables.put("body", CloudWatchUtils.getRequestPayload(query, metricName, metricIdentifier));
    dslEnvVariables.put("serviceName", service);
    dslEnvVariables.put("url", getBaseUrl(region, service));
    return dslEnvVariables;
  }

  public static AWSCredentials getAwsCredentials(AwsConnectorDTO connectorDTO) {
    AWSCredentials awsCredentials = null;
    AwsCredentialType awsCredentialType = connectorDTO.getCredential().getAwsCredentialType();
    if (AwsCredentialType.INHERIT_FROM_DELEGATE.equals(awsCredentialType)
        || AwsCredentialType.IRSA.equals(awsCredentialType)) {
      // TODO: Add handling for STS
      awsCredentials = InstanceProfileCredentialsProvider.getInstance().getCredentials();
    } else {
      AwsManualConfigSpecDTO awsManualConfigSpecDTO = (AwsManualConfigSpecDTO) connectorDTO.getCredential().getConfig();
      String accessKeyId = StringUtils.isEmpty(awsManualConfigSpecDTO.getAccessKey())
          ? String.valueOf(awsManualConfigSpecDTO.getAccessKeyRef().getDecryptedValue())
          : awsManualConfigSpecDTO.getAccessKey();
      String secretKey = String.valueOf(awsManualConfigSpecDTO.getSecretKeyRef().getDecryptedValue());
      awsCredentials = ManualAWSCredentials.builder().accessKeyId(accessKeyId).secretKey(secretKey).build();
    }
    return awsCredentials;
  }

  @Builder
  private static class ManualAWSCredentials implements AWSCredentials {
    private String accessKeyId;
    private String secretKey;
    @Override
    public String getAWSAccessKeyId() {
      return accessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
      return secretKey;
    }
  }
}
