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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import java.util.List;
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
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;

/** Defines Devfile REST API. */
@Api(value = "/devfile", description = "Devfile REST API")
@Path("/devfile")
public class DevfileService extends Service {

  //  private final WorkspaceManager workspaceManager;
  //  private final MachineTokenProvider machineTokenProvider;
  //  private final WorkspaceLinksGenerator linksGenerator;
  //  private final String pluginRegistryUrl;
  //  private final String devfileRegistryUrl;
  //  private final String apiEndpoint;
  //  private final boolean cheWorkspaceAutoStart;
  //  private final FileContentProvider devfileContentProvider;
  //  private final boolean defaultPersistVolumes;
  //  private final Long logLimitBytes;

  @Inject
  public DevfileService(
      //      @Named("che.api") String apiEndpoint,
      //      @Named(CHE_WORKSPACE_AUTO_START) boolean cheWorkspaceAutoStart,
      //      WorkspaceManager workspaceManager,
      //      MachineTokenProvider machineTokenProvider,
      //      WorkspaceLinksGenerator linksGenerator,
      //      @Named(CHE_WORKSPACE_PLUGIN_REGISTRY_URL_PROPERTY) @Nullable String pluginRegistryUrl,
      //      @Named(CHE_WORKSPACE_DEVFILE_REGISTRY_URL_PROPERTY) @Nullable String
      // devfileRegistryUrl,
      //      @Named(CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY) boolean defaultPersistVolumes,
      //      URLFetcher urlFetcher,
      //      @Named(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES) Long logLimitBytes

      ) {
    //    this.apiEndpoint = apiEndpoint;
    //    this.cheWorkspaceAutoStart = cheWorkspaceAutoStart;
    //    this.workspaceManager = workspaceManager;
    //    this.machineTokenProvider = machineTokenProvider;
    //    this.linksGenerator = linksGenerator;
    //    this.pluginRegistryUrl = pluginRegistryUrl;
    //    this.devfileRegistryUrl = devfileRegistryUrl;
    //    this.devfileContentProvider = new URLFileContentProvider(null, urlFetcher);
    //    this.defaultPersistVolumes = defaultPersistVolumes;
    //    this.logLimitBytes = logLimitBytes;
  }

  @POST
  @Consumes({APPLICATION_JSON, "text/yaml", "text/x-yaml"})
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Creates a new workspace based on the Devfile.",
      consumes = "application/json, text/yaml, text/x-yaml",
      produces = APPLICATION_JSON,
      nickname = "createFromDevfile",
      response = WorkspaceConfigDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The workspace successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new workspace"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during the workspace creation"
                + "(e.g. The workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response create(
      @ApiParam(value = "The devfile of the workspace to create", required = true)
          DevfileDto devfile,
      @ApiParam(
              value =
                  "Workspace attribute defined in 'attrName:attrValue' format. "
                      + "The first ':' is considered as attribute name and value separator",
              examples = @Example({@ExampleProperty("attrName:value-with:colon")}))
          @QueryParam("attribute")
          List<String> attrsList,
      @ApiParam(
              value =
                  "The target infrastructure namespace (Kubernetes namespace or OpenShift"
                      + " project) where the workspace should be deployed to when started. This"
                      + " parameter is optional. The workspace creation will fail if the Che server"
                      + " is configured to not allow deploying into that infrastructure namespace.")
          @QueryParam("infrastructure-namespace")
          String infrastructureNamespace,
      @ApiParam(
              "If true then the workspace will be immediately "
                  + "started after it is successfully created")
          @QueryParam("start-after-create")
          @DefaultValue("false")
          Boolean startAfterCreate,
      @ApiParam("Che namespace where workspace should be created") @QueryParam("namespace")
          String namespace,
      @HeaderParam(CONTENT_TYPE) MediaType contentType)
      throws ConflictException, BadRequestException, ForbiddenException, NotFoundException,
          ServerException {
    //    requiredNotNull(devfile, "Devfile");
    //    final Map<String, String> attributes = parseAttrs(attrsList);
    //    if (namespace == null) {
    //      namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    //    }
    //    if (!isNullOrEmpty(infrastructureNamespace)) {
    //      attributes.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, infrastructureNamespace);
    //    }
    //    WorkspaceImpl workspace;
    //    try {
    //      workspace =
    //          workspaceManager.createWorkspace(
    //              devfile,
    //              namespace,
    //              attributes,
    //              // create a new cache for each request so that we don't have to care about
    // lifetime
    //              // of the cache, etc. The content is cached only for the duration of this call
    //              // (i.e. all the validation and provisioning of the devfile will download each
    //              // referenced file only once per request)
    //              FileContentProvider.cached(devfileContentProvider));
    //    } catch (ValidationException x) {
    //      throw new BadRequestException(x.getMessage());
    //    }
    //
    //    if (startAfterCreate) {
    //      workspaceManager.startWorkspace(workspace.getId(), null, new HashMap<>());
    //    }
    // return Response.status(201).entity(asDtoWithLinksAndToken(workspace)).build();
    return Response.status(201).build();
  }

  @GET
  @Path("/{key:.*}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get the workspace by the composite key",
      notes =
          "Composite key can be just workspace ID or in the "
              + "namespace:workspace_name form, where namespace is optional (e.g :workspace_name is valid key too."
              + "namespace/workspace_name form, where namespace can contain '/' character.")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested workspace entity"),
    @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
    @ApiResponse(code = 403, message = "The user is not workspace owner"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto getById(
      @ApiParam(
              value = "Composite key",
              examples =
                  @Example({
                    @ExampleProperty("workspace12345678"),
                    @ExampleProperty("namespace/workspace_name"),
                    @ExampleProperty("namespace_part_1/namespace_part_2/workspace_name")
                  }))
          @PathParam("key")
          String key,
      @ApiParam("Whether to include internal servers into runtime or not")
          @DefaultValue("false")
          @QueryParam("includeInternalServers")
          String includeInternalServers)
      throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
    //    validateKey(key);
    //    boolean bIncludeInternalServers =
    //        isNullOrEmpty(includeInternalServers) || Boolean.parseBoolean(includeInternalServers);
    //    return filterServers(
    //        asDtoWithLinksAndToken(workspaceManager.getWorkspace(key)), bIncludeInternalServers);
    return null;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get workspaces which user can read",
      notes = "This operation can be performed only by authorized user",
      response = WorkspaceDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspaces successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")
  })
  public Response getWorkspaces(
      @ApiParam("The number of the items to skip") @DefaultValue("0") @QueryParam("skipCount")
          Integer skipCount,
      @ApiParam("The limit of the items in the response, default is 30")
          @DefaultValue("30")
          @QueryParam("maxItems")
          Integer maxItems,
      @ApiParam("Workspace status") @QueryParam("status") String status)
      throws ServerException, BadRequestException {
    //    Page<WorkspaceImpl> workspacesPage =
    //        workspaceManager.getWorkspaces(
    //            EnvironmentContext.getCurrent().getSubject().getUserId(), false, maxItems,
    // skipCount);
    //    return Response.ok()
    //        .entity(
    //            workspacesPage
    //                .getItems()
    //                .stream()
    //                .filter(ws -> status == null ||
    // status.equalsIgnoreCase(ws.getStatus().toString()))
    //                .map(DtoConverter::asDto)
    //                .collect(toList()))
    //        .header("Link", createLinkHeader(workspacesPage))
    //        .build();
    return null;
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace by replacing all the existing data with update",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during workspace update"
                + "(e.g. Workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto update(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam(value = "The workspace update", required = true) WorkspaceDto update)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException,
          ConflictException {
    //    checkArgument(
    //        update.getConfig() != null ^ update.getDevfile() != null,
    //        "Required non-null workspace configuration or devfile update but not both");
    //    relativizeRecipeLinks(update.getConfig());
    //    return asDtoWithLinksAndToken(doUpdate(id, update));
    return null;
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
      value = "Removes the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The workspace successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access to remove the workspace"),
    @ApiResponse(code = 404, message = "The workspace doesn't exist"),
    @ApiResponse(code = 409, message = "The workspace is not stopped(has runtime)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void delete(@ApiParam("The workspace id") @PathParam("id") String id)
      throws BadRequestException, ServerException, NotFoundException, ConflictException,
          ForbiddenException {
    // workspaceManager.removeWorkspace(id);
  }
}
