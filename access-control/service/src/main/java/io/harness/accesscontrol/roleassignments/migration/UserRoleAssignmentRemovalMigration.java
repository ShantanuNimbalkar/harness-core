package io.harness.accesscontrol.roleassignments.migration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.harness.accesscontrol.commons.helpers.FeatureFlagHelperService;
import io.harness.accesscontrol.roleassignments.persistence.RoleAssignmentDBO;
import io.harness.accesscontrol.roleassignments.persistence.repositories.RoleAssignmentRepository;
import io.harness.accesscontrol.scopes.core.ScopeService;
import io.harness.accesscontrol.scopes.harness.HarnessScopeLevel;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.migrations.Migration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.List;

import static io.harness.accesscontrol.principals.PrincipalType.USER;
import static io.harness.accesscontrol.resources.resourcegroups.HarnessResourceGroupConstants.DEFAULT_ACCOUNT_LEVEL_RESOURCE_GROUP_IDENTIFIER;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.PL)
public class UserRoleAssignmentRemovalMigration implements Migration {
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final FeatureFlagHelperService featureFlagHelperService;
    private final HashMap<String, Boolean> featureFlagForAccounts;
    private final ScopeService scopeService;
    private static final String ACCOUNT_VIEWER = "_account_viewer";
    private static final String ACCOUNT_BASIC = "_account_basic";

    @Inject
    public UserRoleAssignmentRemovalMigration(RoleAssignmentRepository roleAssignmentRepository,
                                              FeatureFlagHelperService featureFlagHelperService, ScopeService scopeService) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.featureFlagHelperService = featureFlagHelperService;
        this.featureFlagForAccounts = new HashMap<>();
        this.scopeService = scopeService;
    }

    @Override
    public void migrate() {
        log.info("UserRoleAssignmentRemovalMigration starts ...");

        int pageSize = 1000;
        int pageIndex = 0;

        do {
            Pageable pageable = PageRequest.of(pageIndex, pageSize);
            Criteria criteria = Criteria.where(RoleAssignmentDBO.RoleAssignmentDBOKeys.roleIdentifier)
                    .is(ACCOUNT_VIEWER)
                    .and(RoleAssignmentDBO.RoleAssignmentDBOKeys.resourceGroupIdentifier)
                    .is(DEFAULT_ACCOUNT_LEVEL_RESOURCE_GROUP_IDENTIFIER)
                    .and(RoleAssignmentDBO.RoleAssignmentDBOKeys.scopeLevel)
                    .is(HarnessScopeLevel.ACCOUNT.getName())
                    .and(RoleAssignmentDBO.RoleAssignmentDBOKeys.principalType)
                    .is(USER)
                    .and(RoleAssignmentDBO.RoleAssignmentDBOKeys.managed)
                    .is(true);
            pageIndex++;

            List<RoleAssignmentDBO> roleAssignmentList =
                    roleAssignmentRepository
                            .findAll(criteria, pageable, Sort.by(Sort.Direction.ASC, RoleAssignmentDBO.RoleAssignmentDBOKeys.createdAt))
                            .getContent();
            if (isEmpty(roleAssignmentList)) {
                break;
            }

            roleAssignmentRepository.deleteAll(roleAssignmentList);

        } while (true);
        log.info("UserRoleAssignmentRemovalMigration completed.");

    }
}
