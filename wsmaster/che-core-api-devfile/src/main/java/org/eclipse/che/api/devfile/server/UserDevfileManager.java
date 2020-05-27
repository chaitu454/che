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

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.devfile.shared.event.DevfileCreatedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Singleton
public class UserDevfileManager {
  private static final Logger LOG = LoggerFactory.getLogger(UserDevfileManager.class);
  @Inject UserDevfileDao userDevfileDao;
  @Inject EventService eventService;

  /**
   * Stores {@link Devfile} instance
   *
   * @param devfile instance of devfile which would be stored
   * @return new persisted devfile instance
   * @throws ConflictException when any conflict occurs (e.g Devfile with such name already exists
   *     for {@code owner})
   * @throws ServerException when any other error occurs
   */
  public UserDevfileImpl createDevfile(Devfile devfile)
      throws ServerException, NotFoundException, ConflictException {
    requireNonNull(devfile, "Required non-null devfile");
    UserDevfileImpl userDevfile =
        new UserDevfileImpl(NameGenerator.generate("usrdevfile", 16), devfile);
    userDevfile = userDevfileDao.create(userDevfile);
    LOG.info(
        "UserDevfile '{}' with id '{}' created by user '{}'",
        userDevfile.getName(),
        userDevfile.getId(),
        EnvironmentContext.getCurrent().getSubject().getUserName());
    eventService.publish(new DevfileCreatedEvent(userDevfile));
    return userDevfile;
  }
}
