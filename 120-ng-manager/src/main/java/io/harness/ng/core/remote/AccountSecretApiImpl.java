package io.harness.ng.core.remote;

import com.google.inject.Inject;
import io.harness.accesscontrol.acl.api.Resource;
import io.harness.accesscontrol.acl.api.ResourceScope;
import io.harness.exception.InvalidRequestException;
import io.harness.ng.core.api.SecretCrudService;
import io.harness.ng.core.api.impl.SecretPermissionValidator;
import io.harness.ng.core.dto.secrets.SecretDTOV2;
import io.harness.ng.core.dto.secrets.SecretResponseWrapper;
import io.harness.secretmanagerclient.SecretType;
import io.harness.security.SecurityContextBuilder;
import io.harness.spec.server.ng.AccountSecretApi;
import io.harness.spec.server.ng.model.SecretRequest;
import io.harness.spec.server.ng.model.SecretResponse;
import lombok.AllArgsConstructor;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static io.harness.exception.WingsException.USER;
import static io.harness.ng.core.remote.SecretApiMapper.toSecretDto;
import static io.harness.ng.core.remote.SecretApiMapper.toSecretResponse;
import static io.harness.secrets.SecretPermissions.SECRET_DELETE_PERMISSION;
import static io.harness.secrets.SecretPermissions.SECRET_EDIT_PERMISSION;
import static io.harness.secrets.SecretPermissions.SECRET_RESOURCE_TYPE;
import static io.harness.secrets.SecretPermissions.SECRET_VIEW_PERMISSION;
import static io.harness.utils.PageUtils.getNGPageResponse;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class AccountSecretApiImpl implements AccountSecretApi {
  private final SecretCrudService ngSecretService;
  private final SecretPermissionValidator secretPermissionValidator;

  @Override
  public Response createAccountScopedSecret(SecretRequest body, String account, Boolean privateSecret) {
    if (nonNull(body.getSecret().getOrg()) || nonNull(body.getSecret().getProject())) {
      throw new InvalidRequestException("Invalid request, scope in payload and params do not match.", USER);
    }
    return createSecret(account, body, privateSecret);
  }

  @Override
  public Response createAccountScopedSecret(SecretRequest secretRequest, InputStream fileInputStream, String account, Boolean privateSecret) {
    if (nonNull(secretRequest.getSecret().getOrg()) || nonNull(secretRequest.getSecret().getProject())) {
      throw new InvalidRequestException("Invalid request, scope in payload and params do not match.", USER);
    }
    secretPermissionValidator.checkForAccessOrThrow(
            ResourceScope.of(account, secretRequest.getSecret().getOrg(), secretRequest.getSecret().getProject()), Resource.of(SECRET_RESOURCE_TYPE, null),
            SECRET_EDIT_PERMISSION, privateSecret ? SecurityContextBuilder.getPrincipal() : null);
    SecretDTOV2 secretDto = toSecretDto(secretRequest.getSecret());

    if (privateSecret) {
      secretDto.setOwner(SecurityContextBuilder.getPrincipal());
    }

    return Response.ok()
            .entity(ngSecretService.createFile(account, secretDto, fileInputStream))
            .build();
  }

  @Override
  public Response deleteAccountScopedSecret(String secret, String account) {
    return deleteSecret(null, null, secret, account);
  }

  @Override
  public Response getAccountScopedSecret(String secret, String account) {
    return getSecret(null, null, secret, account);
  }

  @Override
  public Response getAccountScopedSecrets(String account, String org, String project, List<String> secret,
      List<String> type, Boolean recursive, String searchTerm, Integer page, Integer limit) {
    return getSecrets(account, org, project, secret, type, recursive, searchTerm, page, limit);
  }

  @Override
  public Response updateAccountScopedSecret(SecretRequest body, String secret, String account) {
    return updateSecret(body, null, null, secret, account);
  }

  @Override
  public Response updateAccountScopedSecret(SecretRequest secretRequest, InputStream fileInputStream, String secret, String account) {
    if (nonNull(secretRequest.getSecret().getOrg()) || nonNull(secretRequest.getSecret().getProject())) {
      throw new InvalidRequestException("Invalid request, scope in payload and params do not match.", USER);
    }
    SecretResponseWrapper secretResponseWrapper = ngSecretService.get(account, null, null, secret).orElse(null);
    secretPermissionValidator.checkForAccessOrThrow(
            ResourceScope.of(account, secretRequest.getSecret().getOrg(), secretRequest.getSecret().getProject()),
            Resource.of(SECRET_RESOURCE_TYPE, secret), SECRET_EDIT_PERMISSION,
            secretResponseWrapper != null ? secretResponseWrapper.getSecret().getOwner() : null);

    SecretDTOV2 secretDto = toSecretDto(secretRequest.getSecret());

    return Response.ok()
            .entity(ngSecretService.updateFile(account, null, null, secret, secretDto, fileInputStream))
            .build();
  }

  @Override
  public Response validateUniqueAccountScopedSecretSlug(String secret, String account) {
    return validateSecretSlug(secret, account, null, null);
  }

  private Response validateSecretSlug(String secret, String account, String org, String project) {
    return Response.ok().entity(ngSecretService.validateTheIdentifierIsUnique(account, org, project, secret)).build();
  }

  private Response updateSecret(SecretRequest body, String org, String project, String secret, String account) {
    SecretResponseWrapper secretResponseWrapper = ngSecretService.get(account, org, project, secret).orElse(null);
    secretPermissionValidator.checkForAccessOrThrow(ResourceScope.of(account, org, project),
        Resource.of(SECRET_RESOURCE_TYPE, secret), SECRET_EDIT_PERMISSION,
        secretResponseWrapper != null ? secretResponseWrapper.getSecret().getOwner() : null);

    SecretResponseWrapper updatedSecret =
        ngSecretService.update(account, org, project, secret, toSecretDto(body.getSecret()));
    return Response.ok().entity(updatedSecret).build();
  }

  private Response deleteSecret(String org, String project, String secret, String account) {
    SecretResponseWrapper secretResponseWrapper = ngSecretService.get(account, org, project, secret).orElse(null);
    secretPermissionValidator.checkForAccessOrThrow(ResourceScope.of(account, org, project),
        Resource.of(SECRET_RESOURCE_TYPE, secret), SECRET_DELETE_PERMISSION,
        secretResponseWrapper != null ? secretResponseWrapper.getSecret().getOwner() : null);
    boolean deleted = ngSecretService.delete(account, org, project, secret);
    if (deleted) {
      return Response.ok().entity(toSecretResponse(secretResponseWrapper)).build();
    }
    throw new NotFoundException(
        format("Secret with identifier [%s] in org [%s] and project [%s] not found", secret, org, project));
  }

  private Response getSecret(String org, String project, String secret, String account) {
    SecretResponseWrapper secretResponseWrapper = ngSecretService.get(account, org, project, secret).orElse(null);
    secretPermissionValidator.checkForAccessOrThrow(ResourceScope.of(account, org, project),
        Resource.of(SECRET_RESOURCE_TYPE, secret), SECRET_VIEW_PERMISSION,
        secretResponseWrapper != null ? secretResponseWrapper.getSecret().getOwner() : null);

    if (nonNull(secretResponseWrapper)) {
      return Response.ok().entity(toSecretResponse(secretResponseWrapper)).build();
    }
    throw new NotFoundException(
        format("Secret with identifier [%s] in org [%s] and project [%s] not found", secret, org, project));
  }

  private Response getSecrets(String account, String org, String project, List<String> secret, List<String> type,
      Boolean recursive, String searchTerm, Integer page, Integer limit) {
    List<SecretType> secretTypes = SecretApiMapper.toSecretTypes(type);

    List<SecretResponseWrapper> content = getNGPageResponse(
        ngSecretService.list(account, org, project, secret, secretTypes, recursive, searchTerm, page, limit, null))
                                              .getContent();

    List<SecretResponse> secretResponse =
        content.stream().map(SecretApiMapper::toSecretResponse).collect(Collectors.toList());
    return Response.ok().entity(secretResponse).build();
  }

  private Response createSecret(String account, SecretRequest body, Boolean privateSecret) {
    secretPermissionValidator.checkForAccessOrThrow(
        ResourceScope.of(account, body.getSecret().getOrg(), body.getSecret().getProject()),
        Resource.of(SECRET_RESOURCE_TYPE, null), SECRET_EDIT_PERMISSION,
        privateSecret ? SecurityContextBuilder.getPrincipal() : null);

    SecretDTOV2 secretDto = toSecretDto(body.getSecret());

    if (TRUE.equals(privateSecret)) {
      secretDto.setOwner(SecurityContextBuilder.getPrincipal());
    }
    SecretResponseWrapper entity = ngSecretService.create(account, secretDto);

    return Response.ok().entity(toSecretResponse(entity)).build();
  }
}
