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
package org.eclipse.che.api.devfile.server.spi;

import com.google.common.annotations.Beta;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;

/** Defines data access object contract for {@code UserDevfileImpl}. */
@Beta
public interface UserDevfileDao {

  /**
   * Creates Devfile.
   *
   * @param devfile devfile to create
   * @return created devfile
   * @throws NullPointerException when {@code devfile} is null
   * @throws ServerException when any other error occurs
   */
  UserDevfileImpl create(UserDevfileImpl devfile) throws ServerException, ConflictException;

  /**
   * Updates devfile to the new entity, using replacement strategy.
   *
   * @param devfile devfile to update
   * @return updated devfile
   * @throws NullPointerException when {@code devfile} is null
   * @throws NotFoundException when given devfile is not found
   * @throws ConflictException when any conflict situation occurs
   * @throws ServerException when any other error occurs
   */
  UserDevfileImpl update(UserDevfileImpl devfile)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Removes devfile.
   *
   * @param id devfile identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String id) throws ServerException;

  /**
   * Gets devfile by identifier.
   *
   * @param id devfile identifier
   * @return devfile instance, never null
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when devfile with given {@code id} is not found
   * @throws ServerException when any other error occurs
   */
  UserDevfileImpl getById(String id) throws NotFoundException, ServerException;

  /**
   * Gets all devfiles which user can read.
   *
   * @param userId user identifier
   * @return list of devfiles which user can read, never null
   * @throws ServerException when any other error occurs during devfile fetching
   */
  Page<UserDevfileImpl> getDevfiles(String userId, int maxItems, long skipCount)
      throws ServerException;
}
