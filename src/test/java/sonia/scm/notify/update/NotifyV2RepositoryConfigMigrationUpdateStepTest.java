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

package sonia.scm.notify.update;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class NotifyV2RepositoryConfigMigrationUpdateStepTest {

  private final static String REPO_NAME = "repo";
  private static final String STORE_NAME = "NotifyConfigurations";

  private final V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  private final ConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  private NotifyV2RepositoryConfigMigrationUpdateStep updateStep;

  @Before
  public void init() {
    updateStep = new NotifyV2RepositoryConfigMigrationUpdateStep(testUtil.getPropertyDAO(), storeFactory);
  }

  @Test
  public void shouldMigratingForRepository() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "notify.contact.repository","true",
        "notify.max.diff.lines", "42",
        "notify.contact.list", "dritte@email.de;echo@off.de;abc@def.de;",
        "notify.email.per.push", "true",
        "notify.use.author.as.from.address", "false"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(getConfigStore().get().getMaxDiffLines()).isEqualTo(42);
    assertThat(getConfigStore().get().isEmailPerPush()).isTrue();
    assertThat(getConfigStore().get().isSendToRepositoryContact()).isTrue();
    assertThat(getConfigStore().get().isUseAuthorAsFromAddress()).isFalse();
    assertThat(getConfigStore().get().getContactList()).contains("abc@def.de");
    assertThat(getConfigStore().get().getContactList()).contains("echo@off.de");
    assertThat(getConfigStore().get().getContactList().size()).isEqualTo(3);
  }

  @Test
  public void shouldMigratingForRepositoryIfMaxDiffLinesAreAnEmptyString() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "notify.contact.repository","true",
        "notify.max.diff.lines", "",
        "notify.contact.list", "dritte@email.de;echo@off.de;abc@def.de;",
        "notify.email.per.push", "true",
        "notify.use.author.as.from.address", "false"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(getConfigStore().get().getMaxDiffLines()).isEqualTo(0);
    assertThat(getConfigStore().get().isEmailPerPush()).isTrue();
    assertThat(getConfigStore().get().isSendToRepositoryContact()).isTrue();
    assertThat(getConfigStore().get().isUseAuthorAsFromAddress()).isFalse();
    assertThat(getConfigStore().get().getContactList()).contains("abc@def.de");
    assertThat(getConfigStore().get().getContactList()).contains("echo@off.de");
    assertThat(getConfigStore().get().getContactList().size()).isEqualTo(3);
  }

  @Test
  public void shouldSkipRepositoriesIfPermissionsAreEmpty() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "any", "value"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(getConfigStore().get()).isNull();
  }

  private ConfigurationStore<NotifyRepositoryConfiguration> getConfigStore() {
    return storeFactory.withType(NotifyRepositoryConfiguration.class).withName(STORE_NAME).forRepository(REPO_NAME).build();
  }
}
