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

package sonia.scm.notify.service;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Lists;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/notify/shiro-001.ini", username = "user_1", password = "secret")
public class NotifyRepositoryConfigurationServiceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  ConfigurationStore<NotifyRepositoryConfiguration> store;
  ConfigurationStoreFactory storeFactory;

  NotifyRepositoryConfigurationService service;
  public static final Repository REPOSITORY = new Repository("id-1", "git", "scm", "hog");

  @Before
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory();
    store = storeFactory.withType(NotifyRepositoryConfiguration.class).withName("NotifyConfigurations").forRepository(REPOSITORY).build();
    service = new NotifyRepositoryConfigurationService(storeFactory, null);
  }

  public NotifyRepositoryConfigurationServiceTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }

  @Test
  @SubjectAware(username = "notify", password = "secret")
  public void shouldStoreConfigsForPrivilegedUser() {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setContactList(Lists.newArrayList("user1", "user2"));
    configuration.setEmailPerPush(true);
    configuration.setMaxDiffLines(10);
    configuration.setSendToRepositoryContact(true);
    configuration.setUseAuthorAsFromAddress(true);
    service.setConfiguration(REPOSITORY, configuration);

    assertThat(store.get()).isSameAs(configuration);
  }

  @Test
  public void shouldFailOnStoringConfigsForNotAdminOrOwnerUsers() {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setContactList(Lists.newArrayList("user1", "user2"));
    configuration.setEmailPerPush(true);
    configuration.setMaxDiffLines(10);
    configuration.setSendToRepositoryContact(true);
    configuration.setUseAuthorAsFromAddress(true);

    assertThatThrownBy(() -> service.setConfiguration(REPOSITORY, configuration))
      .hasMessage("Subject does not have permission [repository:notify:id-1]");
  }

}
