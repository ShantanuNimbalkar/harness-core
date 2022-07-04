/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngsettings.services;

import io.harness.ngsettings.SettingCategory;
import io.harness.ngsettings.dto.SettingBatchResponseDTO;
import io.harness.ngsettings.dto.SettingRequestDTO;
import io.harness.ngsettings.dto.SettingResponseDTO;
import io.harness.ngsettings.dto.SettingValueResponseDTO;
import io.harness.ngsettings.entities.SettingConfiguration;

import java.util.List;

public interface SettingsService {
  List<SettingResponseDTO> list(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, SettingCategory category);
  List<SettingBatchResponseDTO> update(String accountIdentifier, List<SettingRequestDTO> settingRequestDTO);
  SettingValueResponseDTO get(
      String identifier, String accountIdentifier, String orgIdentifier, String projectIdentifier);
  List<SettingConfiguration> listDefaultSettings();
  void deleteConfig(String identifier);
  SettingConfiguration upsertConfig(SettingConfiguration settingConfiguration);
}
