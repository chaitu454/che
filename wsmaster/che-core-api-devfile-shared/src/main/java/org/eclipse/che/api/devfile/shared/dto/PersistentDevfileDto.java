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
import org.eclipse.che.api.core.model.workspace.devfile.PersistentDevfile;
import org.eclipse.che.api.workspace.shared.dto.devfile.ComponentDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileCommandDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PersistentDevfileDto extends PersistentDevfile {

  void setId(String id);

  PersistentDevfileDto withId(String id);

  @Override
  String getApiVersion();

  void setApiVersion(String apiVersion);

  PersistentDevfileDto withApiVersion(String apiVersion);

  @Override
  List<ProjectDto> getProjects();

  void setProjects(List<ProjectDto> projects);

  PersistentDevfileDto withProjects(List<ProjectDto> projects);

  @Override
  List<ComponentDto> getComponents();

  void setComponents(List<ComponentDto> components);

  PersistentDevfileDto withComponents(List<ComponentDto> components);

  @Override
  List<DevfileCommandDto> getCommands();

  void setCommands(List<DevfileCommandDto> commands);

  PersistentDevfileDto withCommands(List<DevfileCommandDto> commands);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  PersistentDevfileDto withAttributes(Map<String, String> attributes);

  @Override
  MetadataDto getMetadata();

  void setMetadata(MetadataDto metadata);

  PersistentDevfileDto withMetadata(MetadataDto metadata);

  Map<String, String> getLinks();

  void setLinks(Map<String, String> links);

  PersistentDevfileDto withLinks(Map<String, String> links);
}
