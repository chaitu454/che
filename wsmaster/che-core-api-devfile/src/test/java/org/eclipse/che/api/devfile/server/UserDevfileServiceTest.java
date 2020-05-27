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

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.Collections;
import java.util.HashSet;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.devfile.DevfileEntityProvider;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link UserDevfileService}. */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UserDevfileServiceTest {

  private static final String CURRENT_USER_ID = "user123";

  ApiExceptionMapper mapper;
  CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());
  private DevfileEntityProvider devfileEntityProvider =
      new DevfileEntityProvider(
          new DevfileParser(
              new DevfileSchemaValidator(new DevfileSchemaProvider()),
              new DevfileIntegrityValidator(Collections.emptyMap())));
  @Mock UserDevfileDao userDevfileDao;
  @Mock UserDevfileManager userDevfileManager;
  @Mock EventService eventService;
  @Mock UserDevfileServiceLinksInjector linksInjector;
  @InjectMocks UserDevfileService userDevfileService;

  @BeforeMethod
  public void setup() {
    when(linksInjector.injectLinks(any(UserDevfileDto.class), any(ServiceContext.class)))
        .thenAnswer((Answer<UserDevfileDto>) invocation -> invocation.getArgument(0));
  }

  @Test
  public void shouldCreateUserDevfile() throws Exception {
    final DevfileDto devfileDto =
        DtoConverter.asDto(TestObjectGenerator.createDevfile("devfile-name"));
    final UserDevfileImpl userDevfileImpl = new UserDevfileImpl(null, devfileDto);

    when(userDevfileManager.createDevfile(any(Devfile.class))).thenReturn(userDevfileImpl);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(devfileDto)
            .when()
            .post(SECURE_PATH + "/userdevfile");

    assertEquals(response.getStatusCode(), 201);
    assertEquals(new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), userDevfileImpl);
    verify(userDevfileManager).createDevfile(any(Devfile.class));
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    final UserDevfileImpl userDevfile = TestObjectGenerator.createUserDevfile();
    when(userDevfileManager.getById(eq("id-22323"))).thenReturn(userDevfile);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/userdevfile/id-22323");

    assertEquals(new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), userDevfile);
    verify(userDevfileManager).getById(eq("id-22323"));
    verify(linksInjector).injectLinks(any(), any());
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.asString(), dtoClass);
  }
}
