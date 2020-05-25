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

import com.google.inject.persist.Transactional;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.PersistentDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.DevfileDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.Pages.iterate;

@Singleton
public class JpaDevfileDao implements DevfileDao {
  private static final Logger LOG = LoggerFactory.getLogger(JpaDevfileDao.class);

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public PersistentDevfileImpl create(PersistentDevfileImpl devfile)
      throws ConflictException, ServerException {
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
    return new PersistentDevfileImpl(devfile);
  }

  @Override
  public PersistentDevfileImpl update(PersistentDevfileImpl update)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(update);
    try {
      return new PersistentDevfileImpl(doUpdate(update));
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
  public PersistentDevfileImpl getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id);
    try {
      final PersistentDevfileImpl devfile =
          managerProvider.get().find(PersistentDevfileImpl.class, id);
      if (devfile == null) {
        throw new NotFoundException(format("Factory with id '%s' doesn't exist", id));
      }
      return new PersistentDevfileImpl(devfile);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public Page<PersistentDevfileImpl> getDevfiles(String userId, int maxItems, long skipCount)
      throws ServerException {
    try {
      final List<PersistentDevfileImpl> list =
          managerProvider.get()
              .createNamedQuery("PersistentDevfile.getAll", PersistentDevfileImpl.class)
              .setMaxResults(maxItems).setFirstResult((int) skipCount).getResultList().stream()
              .map(PersistentDevfileImpl::new)
              .collect(Collectors.toList());
      final long count =
          managerProvider
              .get()
              .createNamedQuery("PersistentDevfile.getDevfilesTotalCount", Long.class)
              .getSingleResult();
      return new Page<>(list, skipCount, maxItems, count);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected void doCreate(PersistentDevfileImpl devfile) {
    final EntityManager manager = managerProvider.get();
    manager.persist(devfile);
    manager.flush();
  }

  @Transactional
  protected PersistentDevfileImpl doUpdate(PersistentDevfileImpl update) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    if (manager.find(DevfileImpl.class, update.getId()) == null) {
      throw new NotFoundException(
          format("Could not update devfile with id %s because it doesn't exist", update.getId()));
    }
    PersistentDevfileImpl merged = manager.merge(update);
    manager.flush();
    return merged;
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    final DevfileImpl devfile = manager.find(DevfileImpl.class, id);
    if (devfile != null) {
      manager.remove(devfile);
      manager.flush();
    }
  }

  @Singleton
  public static class RemoveDevfilesBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private DevfileDao devfileDao;
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
      for (PersistentDevfileImpl devfile :
          iterate(
              (maxItems, skipCount) ->
                  devfileDao.getDevfiles(event.getUser().getId(), maxItems, skipCount))) {
        devfileDao.remove(devfile.getId());
      }
    }
  }
}
