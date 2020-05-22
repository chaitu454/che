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
package org.eclipse.che.api.devfile.server;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevfileManager {
  private static final Logger LOG = LoggerFactory.getLogger(DevfileManager.class);

  /**
   * Stores {@link Devfile} instance
   *
   * @param devfile instance of devfile which would be stored
   * @return new persisted devfile instance
   * @throws ConflictException when any conflict occurs (e.g Devfile with such name already exists
   *     for {@code owner})
   * @throws ServerException when any other error occurs
   * @throws ValidationException when incoming configuration or attributes are not valid
   */
  @Traced
  public WorkspaceImpl createWorkspace(
      Devfile devfile,
      String namespace,
      Map<String, String> attributes,
      FileContentProvider contentProvider)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    requireNonNull(devfile, "Required non-null devfile");
    requireNonNull(namespace, "Required non-null namespace");
    validator.validateAttributes(attributes);

    devfile = generateNameIfNeeded(devfile);

    try {
      devfileIntegrityValidator.validateContentReferences(devfile, contentProvider);
    } catch (DevfileFormatException e) {
      throw new ValidationException(e.getMessage(), e);
    }

    WorkspaceImpl workspace =
        doCreateWorkspace(devfile, accountManager.getByName(namespace), attributes, false);
    TracingTags.WORKSPACE_ID.set(workspace.getId());
    return workspace;
  }
}
