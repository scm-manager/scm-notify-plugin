package sonia.scm.notify.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.notify.service.NotifyRepositoryConfigurationService;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@OpenAPIDefinition(tags = {
  @Tag(name = "Notify Plugin", description = "Notify plugin provided endpoints")
})
@Path(NotifyRepositoryConfigurationResource.PATH)
public class NotifyRepositoryConfigurationResource {
  public static final String PATH = "v2/plugins/notify";

  private NotifyRepositoryConfigurationService service;
  private NotifyRepositoryConfigurationMapper mapper;

  @Inject
  public NotifyRepositoryConfigurationResource(NotifyRepositoryConfigurationService service, NotifyRepositoryConfigurationMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get notify configuration", description = "Returns the notify configuration.", tags = "Notify Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = NotifyRepositoryConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public NotifyRepositoryConfigurationDto get(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    return mapper.using(uriInfo).map(service.getNotifyConfiguration(namespace, name), new NamespaceAndName(namespace, name));
  }


  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update notify configuration", description = "Modifies the notify configuration.", tags = "Notify Plugin")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"authormapping\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void put(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, NotifyRepositoryConfigurationDto configurationDto) {
    service.setConfiguration(namespace, name, mapper.using(uriInfo).map(configurationDto));
  }

}
