package io.harness.delegate.beans.cvng.cloudwatch;

import io.harness.delegate.beans.connector.awsconnector.AwsConnectorDTO;
import io.harness.delegate.beans.connector.newrelic.NewRelicConnectorDTO;

import java.util.HashMap;
import java.util.Map;

public class CloudWatchUtils {
  private static final String ACCOUNTS_BASE_URL = "v1/accounts/";
  private static final String X_QUERY_KEY = "X-Query-Key";

  // Todo
  public static String getBaseUrl(AwsConnectorDTO awsConnectorDTO) {
    return null;
  }
  // Todo
  public static Map<String, String> collectionHeaders(AwsConnectorDTO awsConnectorDTO) {
    //    String apiKey = new String(newRelicConnectorDTO.getApiKeyRef().getDecryptedValue());
    //    Map<String, String> headers = new HashMap<>();
    //    headers.put(X_QUERY_KEY, apiKey);
    //    headers.put("Accept", "application/json");
    //    return headers;
    return null;
  }
}
