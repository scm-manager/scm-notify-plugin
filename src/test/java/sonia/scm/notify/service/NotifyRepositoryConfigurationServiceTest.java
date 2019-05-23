package sonia.scm.notify.service;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Lists;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/notify/shiro-001.ini", username = "user_1", password = "secret")
public class NotifyRepositoryConfigurationServiceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  ConfigurationStore<NotifyRepositoryConfiguration> store;

  ConfigurationStoreFactory storeFactory;


  NotifyRepositoryConfigurationService service;
  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Before
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory(store);
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

    verify(store).set(argThat(repositoryConfiguration -> {
      assertThat(repositoryConfiguration.getContactList()).hasSize(2);
      assertThat(repositoryConfiguration.getMaxDiffLines()).isEqualTo(10);
      return true;
    }));
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

    verify(store, never()).set(any());
  }

}
