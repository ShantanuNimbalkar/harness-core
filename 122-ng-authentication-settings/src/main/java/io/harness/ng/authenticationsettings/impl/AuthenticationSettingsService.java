/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.ng.authenticationsettings.impl;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.authenticationsettings.dtos.AuthenticationSettingsResponse;
import io.harness.ng.authenticationsettings.dtos.mechanisms.LDAPSettings;
import io.harness.ng.authenticationsettings.dtos.mechanisms.OAuthSettings;
import io.harness.ng.core.account.AuthenticationMechanism;
import io.harness.ng.core.user.TwoFactorAdminOverrideSettings;

import software.wings.beans.loginSettings.LoginSettings;
import software.wings.beans.loginSettings.PasswordStrengthPolicy;
import software.wings.helpers.ext.ldap.LdapResponse;
import software.wings.security.authentication.LoginTypeResponse;
import software.wings.security.authentication.SSOConfig;

import java.util.Set;
import javax.validation.constraints.NotNull;
import okhttp3.MultipartBody;

@OwnedBy(HarnessTeam.PL)
public interface AuthenticationSettingsService {
  AuthenticationSettingsResponse getAuthenticationSettings(String accountIdentifier);
  void updateOauthProviders(String accountId, OAuthSettings settings);
  void updateAuthMechanism(String accountId, AuthenticationMechanism authenticationMechanism);
  void removeOauthMechanism(String accountId);
  LoginSettings updateLoginSettings(String loginSettingsId, String accountIdentifier, LoginSettings loginSettings);
  void updateWhitelistedDomains(String accountIdentifier, Set<String> whitelistedDomains);
  SSOConfig uploadSAMLMetadata(@NotNull String accountId, @NotNull MultipartBody.Part inputStream,
      @NotNull String displayName, String groupMembershipAttr, @NotNull Boolean authorizationEnabled, String logoutUrl,
      String entityIdentifier, String samlProviderType, String clientId, String clientSecret);
  SSOConfig updateSAMLMetadata(@NotNull String accountId, MultipartBody.Part inputStream, String displayName,
      String groupMembershipAttr, @NotNull Boolean authorizationEnabled, String logoutUrl, String entityIdentifier,
      String samlProviderType, String clientId, String clientSecret);
  SSOConfig deleteSAMLMetadata(@NotNull String accountIdentifier);
  LoginTypeResponse getSAMLLoginTest(@NotNull String accountIdentifier);
  LDAPSettings getLdapSettings(@NotNull String accountIdentifier);
  LDAPSettings createLdapSettings(@NotNull String accountIdentifier, LDAPSettings ldapSettings);
  LDAPSettings updateLdapSettings(@NotNull String accountIdentifier, LDAPSettings ldapSettings);
  void deleteLdapSettings(@NotNull String accountIdentifier);
  boolean setTwoFactorAuthAtAccountLevel(
      String accountIdentifier, TwoFactorAdminOverrideSettings twoFactorAdminOverrideSettings);
  PasswordStrengthPolicy getPasswordStrengthSettings(String accountIdentifier);
  LdapResponse testLDAPLogin(@NotNull String accountIdentifier, String email, String password);
}
