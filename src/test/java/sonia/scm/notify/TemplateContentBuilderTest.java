package sonia.scm.notify;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.TemplateType;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateContentBuilderTest {

  private static final Repository REPOSITORY = new Repository("id", "git", "space", "X");

  @Mock
  RepositoryServiceFactory repositoryServiceFactory;

  @Mock
  RepositoryService repositoryService;

  @Mock(answer = Answers.RETURNS_SELF)
  ModificationsCommandBuilder modificationsCommandBuilder;

  @Mock(answer = Answers.RETURNS_SELF)
  private DiffCommandBuilder diffCommandBuilder;

  @Mock
  private ScmConfiguration scmConfiguration;
  private Changeset changeset;

  @Mock
  private TemplateEngineFactory engineFactory;
  private TemplateContentBuilder templateContentBuilder;

  private static final String DIFF = "diff --git a/modifiedFile_1.txt b/modifiedFile_1.txt\n" +
    "index 1 100644\n" +
    "--- a/modifiedFile_1.txt\n" +
    "+++ b/modifiedFile_1.txt\n" +
    "@@ -1 +1 @@\n" +
    "-old content\n" +
    "+new content";

  @BeforeEach
  void init() throws IOException {
    when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommandBuilder);
    Modifications modifications = new Modifications(asList("addedFile_1.txt", "addedFile_2.txt"), asList("modifiedFile_1.txt", "modifiedFile_2.txt"), asList("deletedFile_1.txt", "deletedFile_2.txt"));
    when(modificationsCommandBuilder.getModifications()).thenReturn(modifications);
    changeset = new Changeset("1", 1549469211000L, new Person("Arthur Dent", "dent@hitchhiker.com"));
    this.changeset.setDescription("Some changes");
    when(scmConfiguration.getBaseUrl()).thenReturn("http://localhost:8081/scm");
    TemplateEngine engine = createEngine();
    when(engineFactory.getEngineByExtension(any())).thenReturn(engine);
    templateContentBuilder = new TemplateContentBuilder(scmConfiguration, repositoryServiceFactory, engineFactory);
  }

  @Test
  void shouldGeneratePrettyEmail() throws IOException {
    when(repositoryService.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.getContent()).thenReturn(DIFF);

    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(true);
    configuration.setMaxDiffLines(10);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim())
      .contains(changeset.getBranches())
      .contains(changeset.getId())
      .contains(changeset.getDescription())
      .contains(changeset.getAuthor().getName())
      .contains(DIFF.split("\n"))
    ;
  }

  @Test
  void shouldGenerateNonPrettyEmail() throws IOException {
    when(repositoryService.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.getContent()).thenReturn(DIFF);

    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(false);
    configuration.setMaxDiffLines(10);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim())
      .contains(changeset.getBranches())
      .contains(changeset.getId())
      .contains(changeset.getDescription())
      .contains(changeset.getAuthor().getName())
      .contains(DIFF.split("\n"));
  }

  @Test
  void shouldGenerateNonPrettyEmailWithoutDiff() throws IOException {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(false);
    configuration.setMaxDiffLines(0);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim())
      .contains(changeset.getBranches())
      .contains(changeset.getId())
      .contains(changeset.getDescription())
      .contains(changeset.getAuthor().getName())
      .doesNotContain(DIFF.split("\n"))    ;
  }

 @Test
  void shouldGeneratePrettyEmailWithoutDiff() throws IOException {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(true);
    configuration.setMaxDiffLines(0);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim())
      .contains(changeset.getBranches())
      .contains(changeset.getId())
      .contains(changeset.getDescription())
      .contains(changeset.getAuthor().getName())
      .doesNotContain(DIFF.split("\n"))    ;
  }

  private TemplateEngine createEngine() {
    return new TemplateEngine() {
      @Override
      public Template getTemplate(String templatePath) {
        return (writer, model) -> {
          URL resource = TemplateContentBuilderTest.class.getClassLoader().getResource(templatePath);
          Mustache mustache = new DefaultMustacheFactory().compile(resource.getPath());
          mustache.execute(writer, model);
        };
      }

      @Override
      public Template getTemplate(String templateIdentifier, Reader reader) {
        return null;
      }

      @Override
      public TemplateType getType() {
        return null;
      }
    };
  }
}
