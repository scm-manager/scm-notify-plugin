package sonia.scm.notify.api;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.HalAppenderMapper;
import sonia.scm.api.v2.resources.InstantAttributeMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.NamespaceAndName;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class NotifyRepositoryConfigurationMapper extends HalAppenderMapper implements InstantAttributeMapper {

  private LinkBuilder linkBuilder;

  @Mapping(target = "attributes", ignore = true)
  public abstract NotifyRepositoryConfigurationDto map(NotifyRepositoryConfiguration configuration, @Context NamespaceAndName namespaceAndName);

  public abstract NotifyRepositoryConfiguration map(NotifyRepositoryConfigurationDto dto);

  public NotifyRepositoryConfigurationMapper using(UriInfo uriInfo) {
    this.linkBuilder = new LinkBuilder(uriInfo::getBaseUri, NotifyRepositoryConfigurationResource.class);
    return this;
  }

  @AfterMapping
  void addLinks(@MappingTarget NotifyRepositoryConfigurationDto dto, @Context NamespaceAndName namespaceAndName) {
    Links.Builder links = Links.linkingTo();
    links.self(linkBuilder.method("get").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).href());
    links.single(link("update", linkBuilder.method("put").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).href()));
    dto.add(links.build());
  }

}
