/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.notify.api;

import com.google.common.collect.Lists;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfigurationService;
import sonia.scm.web.RestDispatcher;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotifyRepositoryConfigurationResourceTest {

  public static final String CONFIGURATIONS_JSON = "{" +
    "\"contactList\":[\"user1\",\"user2\"]," +
    "\"sendToRepositoryContact\":true," +
    "\"useAuthorAsFromAddress\":true," +
    "\"emailPerPush\":true," +
    "\"maxDiffLines\":10," +
    "\"_links\":{" +
    "\"self\":{" +
    "\"href\":\"/v2/plugins/notify/space/repo\"" +
    "}," +
    "\"update\":{" +
    "\"href\":\"/v2/plugins/notify/space/repo\"}" +
    "}}";
  private NotifyRepositoryConfigurationResource resource;

  @Mock
  NotifyRepositoryConfigurationService service;

  private NotifyRepositoryConfigurationMapper mapper = new NotifyRepositoryConfigurationMapperImpl();

  private RestDispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();


  @BeforeEach
  public void init() {
    resource = new NotifyRepositoryConfigurationResource(service, mapper);
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  public void shouldGetNotifyRepositoryConfigurations() throws URISyntaxException, UnsupportedEncodingException {
    NotifyRepositoryConfiguration configuration = createConfigs();

    when(service.getNotifyConfiguration("space", "repo")).thenReturn(configuration);

    MockHttpRequest request = MockHttpRequest
      .get("/" + NotifyRepositoryConfigurationResource.PATH + "/space/repo")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .isEqualTo(CONFIGURATIONS_JSON);
  }

  private NotifyRepositoryConfiguration createConfigs() {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setContactList(Lists.newArrayList("user1", "user2"));
    configuration.setEmailPerPush(true);
    configuration.setMaxDiffLines(10);
    configuration.setSendToRepositoryContact(true);
    configuration.setUseAuthorAsFromAddress(true);
    return configuration;
  }

  @Test
  public void shouldPUTNotifyRepositoryConfigurations() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .put("/" + NotifyRepositoryConfigurationResource.PATH + "/space/repo")
      .contentType(MediaType.APPLICATION_JSON)
      .content(CONFIGURATIONS_JSON.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(service).setConfiguration(eq("space"), eq("repo"), argThat(repositoryConfiguration -> {
    NotifyRepositoryConfiguration configuration = createConfigs();

      assertThat(repositoryConfiguration).isEqualToComparingFieldByFieldRecursively(configuration);
      return true;
    }));
  }

}
