/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.notification.channelDetails;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.notification.Team;
import io.harness.notification.channeldetails.MSTeamChannel;
import io.harness.notification.channeldetails.NotificationChannel;
import io.harness.pms.contracts.ambiance.Ambiance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@OwnedBy(HarnessTeam.PIPELINE)
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(NotificationChannelType.MSTEAMS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsMSTeamChannel extends PmsNotificationChannel {
  List<String> msTeamKeys;
  List<String> userGroups;

  @Override
  public NotificationChannel toNotificationChannel(String accountId, String orgIdentifier, String projectIdentifier,
      String templateId, Map<String, String> templateData, Ambiance ambiance) {
    return MSTeamChannel.builder()
        .msTeamKeys(msTeamKeys)
        .accountId(accountId)
        .team(Team.PIPELINE)
        .orgIdentifier(orgIdentifier)
        .projectIdentifier(projectIdentifier)
        .expressionFunctorToken(ambiance.getExpressionFunctorToken())
        .templateData(templateData)
        .templateId(templateId)
        .userGroups(
            userGroups.stream()
                .map(e -> NotificationChannelUtils.getUserGroups(e, accountId, orgIdentifier, projectIdentifier))
                .collect(Collectors.toList()))
        .build();
  }
}
