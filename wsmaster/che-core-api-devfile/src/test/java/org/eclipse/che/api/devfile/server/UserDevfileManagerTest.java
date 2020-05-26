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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.devfile.server.spi.tck.UserDevfileDaoTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
public class UserDevfileManagerTest {
  @Mock UserDevfileDao userDevfileDao;
  @Mock EventService eventService;
  @InjectMocks UserDevfileManager userDevfileManager;

  @Captor private ArgumentCaptor<UserDevfileImpl> userDevfileArgumentCaptor;

  @BeforeMethod
  public void setup() throws ServerException, ConflictException {
    when(userDevfileDao.create(any(UserDevfileImpl.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
  }

  @Test
  public void shouldGenerateUserDevfileIdOnCreation() throws Exception {
    final UserDevfileImpl factory = UserDevfileDaoTest.createUserDevfile();

    userDevfileManager.createDevfile(factory);
    verify(userDevfileDao).create(userDevfileArgumentCaptor.capture());
    assertFalse(isNullOrEmpty(userDevfileArgumentCaptor.getValue().getId()));
  }
}
