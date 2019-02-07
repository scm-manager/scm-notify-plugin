package sonia.scm.notify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreemarkerContentBuilderTest {

  public static final Repository REPOSITORY = new Repository("id", "git", "space", "X");
  @Mock
  RepositoryServiceFactory repositoryServiceFactory;
  @Mock
  RepositoryService repositoryService;
  @Mock(answer = Answers.RETURNS_SELF)
  ModificationsCommandBuilder modificationsCommandBuilder;

  @Test
  void x() throws IOException {
    when(repositoryServiceFactory.create(any(Repository.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    });
    when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommandBuilder);
    when(modificationsCommandBuilder.getModifications()).thenReturn(new Modifications(asList("123")));

    Changeset changeset1 = new Changeset("1", 1549469211000L, new Person("Arthur Dent", "dent@hitchhiker.com"));
    changeset1.setDescription("Some changes");
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setContactList(asList("ich@du.er"));
    Content content = new FreemarkerContentBuilder(null, repositoryServiceFactory).createContent(REPOSITORY, configuration, new Changeset[] {changeset1});
    System.out.println(content.getContent());
  }
}
