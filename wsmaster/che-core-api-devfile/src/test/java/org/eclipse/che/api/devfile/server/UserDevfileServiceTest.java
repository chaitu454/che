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

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
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
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** Tests for {@link UserDevfileService}. */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UserDevfileServiceTest {

  private static final String CURRENT_USER_ID = "user123";

  private final String USER_DEVFILE_ID = NameGenerator.generate("usrd", 16);
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
    lenient()
        .when(linksInjector.injectLinks(any(UserDevfileDto.class), any(ServiceContext.class)))
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

  @Test
  public void shouldThrowNotFoundExceptionWhenUserDevfileIsNotExistOnGetById() throws Exception {

    final String errMessage = format("UserDevfile with id %s is not found", USER_DEVFILE_ID);
    doThrow(new NotFoundException(errMessage)).when(userDevfileManager).getById(anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .expect()
            .statusCode(404)
            .when()
            .get(SECURE_PATH + "/userdevfile/" + USER_DEVFILE_ID);

    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), errMessage);
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingUserDevfile() throws Exception {
    // given
    final DevfileDto devfileDto =
        DtoConverter.asDto(TestObjectGenerator.createDevfile("devfile-name"));

    doThrow(new NotFoundException(format("User devfile with id %s is not found.", USER_DEVFILE_ID)))
        .when(userDevfileManager)
        .updateUserDevfile(any(UserDevfile.class));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(JsonHelper.toJson(devfileDto))
            .when()
            .put(SECURE_PATH + "/userdevfile/" + USER_DEVFILE_ID);
    // then
    assertEquals(response.getStatusCode(), 404);
    assertEquals(
        unwrapDto(response, ServiceError.class).getMessage(),
        format("User devfile with id %s is not found.", USER_DEVFILE_ID));
  }

  @Test
  public void shouldBeAbleToUpdateUserDevfile() throws Exception {
    // given
    final DevfileDto devfileDto =
            DtoConverter.asDto(TestObjectGenerator.createDevfile("devfile-name"));

    doThrow(new NotFoundException(format("User devfile with id %s is not found.", USER_DEVFILE_ID)))
            .when(userDevfileManager)
            .updateUserDevfile(any(UserDevfile.class));
    // when
    final Response response =
            given()
                    .auth()
                    .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                    .contentType(APPLICATION_JSON)
                    .body(JsonHelper.toJson(devfileDto))
                    .when()
                    .put(SECURE_PATH + "/userdevfile/" + USER_DEVFILE_ID);
    // then
    assertEquals(response.getStatusCode(), 404);
    assertEquals(
            unwrapDto(response, ServiceError.class).getMessage(),
            format("User devfile with id %s is not found.", USER_DEVFILE_ID));
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.asString(), dtoClass);
  }
}
