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

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.devfile.server.DtoConverter.asDto;

import com.google.common.annotations.Beta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;

/** Defines Persistent Devfile REST API. */
@Api(value = "/userdevfile", description = "Persistent Devfile REST API")
@Path("/userdevfile")
@Beta
public class UserDevfileService extends Service {
  @Inject UserDevfileManager userDevfileManager;
  @Inject UserDevfileServiceLinksInjector linksInjector;

  @POST
  @Consumes({APPLICATION_JSON, "text/yaml", "text/x-yaml"})
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Creates a new persistent Devfile",
      consumes = "application/json, text/yaml, text/x-yaml",
      produces = APPLICATION_JSON,
      nickname = "create",
      response = UserDevfileDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The devfile successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new devfile"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during the devfile creation"
                + "(e.g. The devfile with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response create(
      @ApiParam(value = "The devfile of the workspace to create", required = true)
          DevfileDto devfile,
      @HeaderParam(CONTENT_TYPE) MediaType contentType)
      throws ConflictException, BadRequestException, ForbiddenException, NotFoundException,
          ServerException {
    requiredNotNull(devfile, "Devfile");
    return Response.status(201)
        .entity(
            linksInjector.injectLinks(
                asDto(userDevfileManager.createDevfile(devfile)), getServiceContext()))
        .build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get factory by its identifier")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested workspace entity"),
    @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
    @ApiResponse(code = 403, message = "The user is not allowed to read devfile"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public UserDevfileDto getById(
      @ApiParam(value = "UserDevfile identifier") @PathParam("id") String id)
      throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
    requiredNotNull(id, "id");
    return linksInjector.injectLinks(asDto(userDevfileManager.getById(id)), getServiceContext());
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get devfiles which user can read",
      notes = "This operation can be performed only by authorized user",
      response = UserDevfileDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The devfiles successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred during devfiles fetching")
  })
  public Response getWorkspaces(
      @ApiParam("The number of the items to skip") @DefaultValue("0") @QueryParam("skipCount")
          Integer skipCount,
      @ApiParam("The limit of the items in the response, default is 30")
          @DefaultValue("30")
          @QueryParam("maxItems")
          Integer maxItems)
      throws ServerException, BadRequestException {
    return null;
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update the devfile by replacing all the existing data with update")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The devfile successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the devfile"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during devfile update"
                + "(e.g. Workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto update(
      @ApiParam("The devfile id") @PathParam("id") String id,
      @ApiParam(value = "The devfile update", required = true) UserDevfileDto update)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException,
          ConflictException {
    return null;
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Removes the devfile")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The devfile successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access to remove the devfile"),
    @ApiResponse(code = 404, message = "The devfile doesn't exist"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void delete(@ApiParam("The devfile id") @PathParam("id") String id)
      throws BadRequestException, ServerException, NotFoundException, ConflictException,
          ForbiddenException {}

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }
}
