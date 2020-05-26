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
package org.eclipse.che.api.devfile.server.jpa;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.Pages.iterate;

import com.google.common.annotations.Beta;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Beta
public class JpaUserDevfileDao implements UserDevfileDao {
  private static final Logger LOG = LoggerFactory.getLogger(JpaUserDevfileDao.class);

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public UserDevfileImpl create(UserDevfileImpl devfile) throws ConflictException, ServerException {
    requireNonNull(devfile);
    try {
      doCreate(devfile);
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Factory with name '%s' already exists for current user", devfile.getName()));
    } catch (IntegrityConstraintViolationException ex) {
      throw new ConflictException(
          "Could not create devfile with creator that refers on non-existent user");
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
    return new UserDevfileImpl(devfile);
  }

  @Override
  public UserDevfileImpl update(UserDevfileImpl update)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(update);
    try {
      return new UserDevfileImpl(doUpdate(update));
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Factory with name '%s' already exists for current user", update.getName()));
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id);
    try {
      doRemove(id);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public UserDevfileImpl getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id);
    try {
      final UserDevfileImpl devfile = managerProvider.get().find(UserDevfileImpl.class, id);
      if (devfile == null) {
        throw new NotFoundException(format("Factory with id '%s' doesn't exist", id));
      }
      return new UserDevfileImpl(devfile);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public Page<UserDevfileImpl> getDevfiles(String userId, int maxItems, long skipCount)
      throws ServerException {
    try {
      final List<UserDevfileImpl> list =
          managerProvider
              .get()
              .createNamedQuery("UserDevfile.getAll", UserDevfileImpl.class)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(UserDevfileImpl::new)
              .collect(Collectors.toList());
      final long count =
          managerProvider
              .get()
              .createNamedQuery("UserDevfile.getDevfilesTotalCount", Long.class)
              .getSingleResult();
      return new Page<>(list, skipCount, maxItems, count);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected void doCreate(UserDevfileImpl devfile) {
    final EntityManager manager = managerProvider.get();
    manager.persist(devfile);
    manager.flush();
  }

  @Transactional
  protected UserDevfileImpl doUpdate(UserDevfileImpl update) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    if (manager.find(UserDevfileImpl.class, update.getId()) == null) {
      throw new NotFoundException(
          format("Could not update devfile with id %s because it doesn't exist", update.getId()));
    }
    UserDevfileImpl merged = manager.merge(update);
    manager.flush();
    return merged;
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    final UserDevfileImpl devfile = manager.find(UserDevfileImpl.class, id);
    if (devfile != null) {
      manager.remove(devfile);
      manager.flush();
    }
  }

  @Singleton
  public static class RemoveDevfilesBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private UserDevfileDao userDevfileDao;
    @Inject private EventService eventService;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeUserRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeUserRemovedEvent event) throws ServerException {
      for (UserDevfileImpl devfile :
          iterate(
              (maxItems, skipCount) ->
                  userDevfileDao.getDevfiles(event.getUser().getId(), maxItems, skipCount))) {
        userDevfileDao.remove(devfile.getId());
      }
    }
  }
}
