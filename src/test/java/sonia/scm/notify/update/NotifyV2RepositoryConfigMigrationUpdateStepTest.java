/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
