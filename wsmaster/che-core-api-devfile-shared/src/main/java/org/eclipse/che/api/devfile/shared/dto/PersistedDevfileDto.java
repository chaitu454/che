/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.shared.dto;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.PersistedDevfile;
import org.eclipse.che.api.workspace.shared.dto.devfile.ComponentDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileCommandDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PersistedDevfileDto extends PersistedDevfile {

  void setId(String id);

  PersistedDevfileDto withId(String id);

  @Override
  String getApiVersion();

  void setApiVersion(String apiVersion);

  PersistedDevfileDto withApiVersion(String apiVersion);

  @Override
  List<ProjectDto> getProjects();

  void setProjects(List<ProjectDto> projects);

  PersistedDevfileDto withProjects(List<ProjectDto> projects);

  @Override
  List<ComponentDto> getComponents();

  void setComponents(List<ComponentDto> components);

  PersistedDevfileDto withComponents(List<ComponentDto> components);

  @Override
  List<DevfileCommandDto> getCommands();

  void setCommands(List<DevfileCommandDto> commands);

  PersistedDevfileDto withCommands(List<DevfileCommandDto> commands);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  PersistedDevfileDto withAttributes(Map<String, String> attributes);

  @Override
  MetadataDto getMetadata();

  void setMetadata(MetadataDto metadata);

  PersistedDevfileDto withMetadata(MetadataDto metadata);

  Map<String, String> getLinks();

  void setLinks(Map<String, String> links);

  PersistedDevfileDto withLinks(Map<String, String> links);
}
