package sonia.scm.notify;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
  private final static String NOT_PRETTY_OUTPUT_WITHOUT_DIFF = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional //EN\">\n" +
    "<html>\n" +
    "<head>\n" +
    "    <title>[X][default] 1</title>\n" +
    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
    "    <style type=\"text/css\">\n" +
    "        body {\n" +
    "            background-color: #ffffff;\n" +
    "            margin: 10px;\n" +
    "            color: #202020;\n" +
    "            font-family: Verdana,Helvetica,Arial,sans-serif;\n" +
    "            font-size: 75%;\n" +
    "        }\n" +
    "        h1, h2 {\n" +
    "            font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;\n" +
    "            font-weight: bold;\n" +
    "            color: #D20005;\n" +
    "            padding: 0;\n" +
    "        }\n" +
    "        h1 {\n" +
    "            font-size: 18px;\n" +
    "            border-bottom: 1px solid #AFAFAF;\n" +
    "            margin: 0 0 5px 0;\n" +
    "        }\n" +
    "        h2 {\n" +
    "            font-size: 14px;\n" +
    "            margin: 0 0 2px 0;\n" +
    "        }\n" +
    "        table {\n" +
    "            border: 0 none;\n" +
    "            border-collapse: collapse;\n" +
    "            font-size: 100%;\n" +
    "            margin: 20px 0;\n" +
    "            padding: 20px;\n" +
    "            width: 100%;\n" +
    "        }\n" +
    "        tr {\n" +
    "            border: 0;\n" +
    "        }\n" +
    "        td {\n" +
    "            padding: 3px;\n" +
    "            vertical-align: top;\n" +
    "            text-align: left;\n" +
    "            border: 0;\n" +
    "        }\n" +
    "        a {\n" +
    "            color: #045491;\n" +
    "            font-weight: bold;\n" +
    "            text-decoration: none;\n" +
    "        }\n" +
    "    </style>\n" +
    "</head>\n" +
    "<body>\n" +
    "    <h2>Branch: default</h2>\n" +
    "\n" +
    "<table>\n" +
    "        <tr>\n" +
    "            <td style=\"width: 60px;\">\n" +
    "                <a href=\"http://localhost:8081/scm/repo/space/X/changeset/1\" target=\"_blank\">\n" +
    "          1\n" +
    "                </a>\n" +
    "            </td>\n" +
    "            <td style=\"width: 150px;\">\n" +
    "            Wed Feb 06 17:06:51 CET 2019\n" +
    "            </td>\n" +
    "            <td>\n" +
    "            Arthur Dent\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "        <tr>\n" +
    "            <td colspan=\"3\">\n" +
    "                <pre>Some changes</pre>\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "        <tr>\n" +
    "            <td colspan=\"3\">\n" +
    "                    <span>A addedFile_1.txt</span><br />\n" +
    "                    <span>A addedFile_2.txt</span><br />\n" +
    "                    <span>M modifiedFile_1.txt</span><br />\n" +
    "                    <span>M modifiedFile_2.txt</span><br />\n" +
    "                    <span>R deletedFile_1.txt</span><br />\n" +
    "                    <span>R deletedFile_2.txt</span><br />\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "</table>\n" +
    "\n" +
    "\n" +
    "</body>\n" +
    "</html>";

  private final static String NOT_PRETTY_OUTPUT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional //EN\">\n" +
    "<html>\n" +
    "<head>\n" +
    "    <title>[X][default] 1</title>\n" +
    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
    "    <style type=\"text/css\">\n" +
    "        body {\n" +
    "            background-color: #ffffff;\n" +
    "            margin: 10px;\n" +
    "            color: #202020;\n" +
    "            font-family: Verdana,Helvetica,Arial,sans-serif;\n" +
    "            font-size: 75%;\n" +
    "        }\n" +
    "        h1, h2 {\n" +
    "            font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;\n" +
    "            font-weight: bold;\n" +
    "            color: #D20005;\n" +
    "            padding: 0;\n" +
    "        }\n" +
    "        h1 {\n" +
    "            font-size: 18px;\n" +
    "            border-bottom: 1px solid #AFAFAF;\n" +
    "            margin: 0 0 5px 0;\n" +
    "        }\n" +
    "        h2 {\n" +
    "            font-size: 14px;\n" +
    "            margin: 0 0 2px 0;\n" +
    "        }\n" +
    "        table {\n" +
    "            border: 0 none;\n" +
    "            border-collapse: collapse;\n" +
    "            font-size: 100%;\n" +
    "            margin: 20px 0;\n" +
    "            padding: 20px;\n" +
    "            width: 100%;\n" +
    "        }\n" +
    "        tr {\n" +
    "            border: 0;\n" +
    "        }\n" +
    "        td {\n" +
    "            padding: 3px;\n" +
    "            vertical-align: top;\n" +
    "            text-align: left;\n" +
    "            border: 0;\n" +
    "        }\n" +
    "        a {\n" +
    "            color: #045491;\n" +
    "            font-weight: bold;\n" +
    "            text-decoration: none;\n" +
    "        }\n" +
    "    </style>\n" +
    "</head>\n" +
    "<body>\n" +
    "    <h2>Branch: default</h2>\n" +
    "\n" +
    "<table>\n" +
    "        <tr>\n" +
    "            <td style=\"width: 60px;\">\n" +
    "                <a href=\"http://localhost:8081/scm/repo/space/X/changeset/1\" target=\"_blank\">\n" +
    "          1\n" +
    "                </a>\n" +
    "            </td>\n" +
    "            <td style=\"width: 150px;\">\n" +
    "            Wed Feb 06 17:06:51 CET 2019\n" +
    "            </td>\n" +
    "            <td>\n" +
    "            Arthur Dent\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "        <tr>\n" +
    "            <td colspan=\"3\">\n" +
    "                <pre>Some changes</pre>\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "        <tr>\n" +
    "            <td colspan=\"3\">\n" +
    "                    <span>A addedFile_1.txt</span><br />\n" +
    "                    <span>A addedFile_2.txt</span><br />\n" +
    "                    <span>M modifiedFile_1.txt</span><br />\n" +
    "                    <span>M modifiedFile_2.txt</span><br />\n" +
    "                    <span>R deletedFile_1.txt</span><br />\n" +
    "                    <span>R deletedFile_2.txt</span><br />\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "            <tr>\n" +
    "                <td colspan=\"3\">\n" +
    "                    <pre>diff --git a/modifiedFile_1.txt b/modifiedFile_1.txt&#10;index 1 100644&#10;--- a/modifiedFile_1.txt&#10;+++ b/modifiedFile_1.txt&#10;@@ -1 +1 @@&#10;-old content&#10;+new content</pre>\n" +
    "                </td>\n" +
    "            </tr>\n" +
    "</table>\n" +
    "\n" +
    "\n" +
    "</body>\n" +
    "</html>";


  private final static String PRETTY_OUTPUT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional //EN\">\n" +
    "<html>\n" +
    "  <head>\n" +
    "    <title>[X][default] 1</title>\n" +
    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
    "    <style type=\"text/css\">\n" +
    "      body {\n" +
    "        background-color: #ffffff;\n" +
    "        margin: 10px;\n" +
    "        color: #202020;\n" +
    "        font-family: Verdana,Helvetica,Arial,sans-serif;\n" +
    "        font-size: 75%; \n" +
    "      }\n" +
    "      h1, h2 {\n" +
    "       font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;\n" +
    "       font-weight: bold;\n" +
    "       color: #D20005;\n" +
    "       padding: 0;\n" +
    "      }      \n" +
    "      h1 {\n" +
    "       font-size: 18px;\n" +
    "       border-bottom: 1px solid #AFAFAF;\n" +
    "       margin: 0 0 5px 0;\n" +
    "      }\n" +
    "      h2 {\n" +
    "       font-size: 14px;\n" +
    "       margin: 0 0 2px 0;\n" +
    "      }\n" +
    "      table {\n" +
    "        border: 0 none;\n" +
    "        border-collapse: collapse;\n" +
    "        font-size: 100%;\n" +
    "        margin: 20px 0;\n" +
    "        padding: 20px;\n" +
    "        width: 100%;\n" +
    "      }\n" +
    "      tr {\n" +
    "        border: 0;\n" +
    "      }\n" +
    "      td {\n" +
    "        padding: 3px;\n" +
    "        vertical-align: top;\n" +
    "        text-align: left;\n" +
    "        border: 0;\n" +
    "      }\n" +
    "      a {\n" +
    "        color: #045491;\n" +
    "        font-weight: bold;\n" +
    "        text-decoration: none;\n" +
    "      }\n" +
    "      body {\n" +
    "          text-align: center;\n" +
    "      }\n" +
    "      .wrapper {\n" +
    "          display: inline-block;\n" +
    "          margin-top: 1em;\n" +
    "          min-width: 800px;\n" +
    "          text-align: left;\n" +
    "          line-height: 1.5;\n" +
    "      }\n" +
    "      h2 {\n" +
    "          background: #eaeaea;\n" +
    "          background: -moz-linear-gradient(#fafafa, #eaeaea);\n" +
    "          background: -webkit-linear-gradient(#fafafa, #eaeaea);\n" +
    "          -ms-filter: \"progid:DXImageTransform.Microsoft.gradient(startColorstr='#fafafa',endColorstr='#eaeaea')\";\n" +
    "          border-bottom: 1px solid #d8d8d8;\n" +
    "          color: #555;\n" +
    "          font: 14px sans-serif;\n" +
    "          overflow: hidden;\n" +
    "          padding: 10px 6px;\n" +
    "          text-shadow: 0 1px 0 white;\n" +
    "          margin: 0;\n" +
    "      }\n" +
    "      h2.branch {\n" +
    "          border: 1px solid #d8d8d8;\n" +
    "      }\n" +
    "      .file-diff {\n" +
    "          border: 1px solid #d8d8d8;\n" +
    "          margin-bottom: 1em;\n" +
    "          overflow: auto;\n" +
    "          padding: 0 0;\n" +
    "          line-height: 1.4em;\n" +
    "      }\n" +
    "      .file-diff > div {\n" +
    "          width: 100%:\n" +
    "      }\n" +
    "      pre {\n" +
    "          margin: 0;\n" +
    "          font-family: \"Bitstream Vera Sans Mono\", Courier, monospace;\n" +
    "          font-size: 12px;\n" +
    "          line-height: 1.4em;\n" +
    "          text-indent: 0.5em;\n" +
    "      }\n" +
    "      .file {\n" +
    "          color: #aaa;\n" +
    "      }\n" +
    "      .delete {\n" +
    "          background-color: #fdd;\n" +
    "      }\n" +
    "      .insert {\n" +
    "          background-color: #dfd;\n" +
    "      }\n" +
    "      .info {\n" +
    "          color: #a0b;\n" +
    "      }\n" +
    "    </style>\n" +
    "  </head>\n" +
    "  <body style=\"background-color: #ffffff;margin: 10px;color: #202020;font-family: Verdana,Helvetica,Arial,sans-serif;font-size: 75%;\">\n" +
    "      <h2 class=\"branch\" style=\"font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;font-weight: bold;color: #555;padding: 10px 6px;font-size: 14px;margin: 0;background: -webkit-linear-gradient(#fafafa, #eaeaea);-ms-filter: &quot;progid:DXImageTransform.Microsoft.gradient(startColorstr='#fafafa',endColorstr='#eaeaea')&quot;;border: 1px solid #d8d8d8;font: 14px sans-serif;overflow: hidden;text-shadow: 0 1px 0 white;text-align: center;\">Branch: default</h2>\n" +
    "\n" +
    "\n" +
    "\n" +
    " <table style=\"border: 0 none;border-collapse: collapse;font-size: 100%;margin: 20px 0;padding: 20px;width: 100%;\">\n" +
    "        <tr style=\"border: 0;\">\n" +
    "           <td style=\"width: 60px;padding: 3px;vertical-align: top;border: 0;text-align: left;\">\n" +
    "             <a href=\"http://localhost:8081/scm/repo/space/X/changeset/1\" target=\"_blank\" style=\"color: #045491;font-weight: bold;text-decoration: none;\">\n" +
    "               1\n" +
    "             </a>\n" +
    "            </td>\n" +
    "            <td style=\"width: 150px;padding: 3px;vertical-align: top;border: 0;text-align: left;\">\n" +
    "                \n" +
    "            </td>\n" +
    "             <td style=\"padding: 3px;vertical-align: top;border: 0;text-align: left;\">\n" +
    "                  Arthur Dent\n" +
    "              </td>\n" +
    "        </tr>\n" +
    "        <tr style=\"border: 0;\">\n" +
    "          <td colspan=\"3\" style=\"padding: 3px;vertical-align: top;border: 0;text-align: left;\">\n" +
    "            <pre style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;\">Some changes</pre>\n" +
    "          </td>\n" +
    "        </tr>\n" +
    "         <tr style=\"border: 0;\">\n" +
    "            <td colspan=\"3\" style=\"padding: 3px;vertical-align: top;border: 0;text-align: left;\">\n" +
    "                    <span>A addedFile_1.txt</span><br />\n" +
    "                    <span>A addedFile_2.txt</span><br />\n" +
    "                    <span>M modifiedFile_1.txt</span><br />\n" +
    "                    <span>M modifiedFile_2.txt</span><br />\n" +
    "                    <span>R deletedFile_1.txt</span><br />\n" +
    "                    <span>R deletedFile_2.txt</span><br />\n" +
    "            </td>\n" +
    "        </tr>\n" +
    "           <tr style=\"border: 0;\">\n" +
    "                <td colspan=\"3\">\n" +
    "                  <div class=\"wrapper\" style=\"display: inline-block;margin-top: 1em;min-width: 800px;text-align: left;\">\n" +
    "                    <div class=\"file-diff\" style=\"border: 1px solid #d8d8d8;margin-bottom: 1em;overflow: auto;padding: 0 0;\"><h2 style=\"font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;font-weight: bold;color: #555;padding: 10px 6px;font-size: 14px;margin: 0;background: -webkit-linear-gradient(#fafafa, #eaeaea);-ms-filter: &quot;progid:DXImageTransform.Microsoft.gradient(startColorstr='#fafafa',endColorstr='#eaeaea')&quot;;border: 0px solid #d8d8d8;border-bottom: 1px solid #d8d8d8;font: 14px sans-serif;overflow: hidden;text-shadow: 0 1px 0 white;text-align: left;\">modifiedFile_1.txt</h2><pre class = \"file\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;color: #aaa;\">diff --git a/modifiedFile_1.txt b/modifiedFile_1.txt</pre>\n" +
    "<pre class = \"file\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;color: #aaa;\">index 1 100644</pre>\n" +
    "<pre class = \"delete\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;background-color: #fdd;\">--- a/modifiedFile_1.txt</pre>\n" +
    "<pre class = \"insert\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;background-color: #dfd;\">+++ b/modifiedFile_1.txt</pre>\n" +
    "<pre class = \"info\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;color: #a0b;\">@@ -1 +1 @@</pre>\n" +
    "<pre class = \"delete\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;background-color: #fdd;\">-old content</pre>\n" +
    "<pre class = \"insert\" style=\"margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;background-color: #dfd;\">+new content</pre></div>\n" +
    "                  </div>\n" +
    "                </td>\n" +
    "            </tr>\n" +
    "</table>\n" +
    "</body>\n" +
    "</html>\n" +
    "\n";

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
  @Disabled
  void shouldGenerateNonPrettyEmail() throws IOException {
    when(repositoryService.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.getContent()).thenReturn(DIFF);

    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(false);
    configuration.setMaxDiffLines(10);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim()).isEqualTo(NOT_PRETTY_OUTPUT.trim());
  }

  @Test
  @Disabled
  void shouldGenerateNonPrettyEmailWithoutDiff() throws IOException {
    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(false);
    configuration.setMaxDiffLines(0);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim()).isEqualTo(NOT_PRETTY_OUTPUT_WITHOUT_DIFF.trim());
  }

  @Test
  void shouldGeneratePrettyEmail() throws IOException {
    when(repositoryService.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.getContent()).thenReturn(DIFF);

    NotifyRepositoryConfiguration configuration = new NotifyRepositoryConfiguration();
    configuration.setUsePrettyDiff(true);
    configuration.setMaxDiffLines(10);

    Content content = templateContentBuilder.createContent(REPOSITORY, configuration, changeset);

    assertThat(content.getContent().trim()).isEqualTo(PRETTY_OUTPUT.trim());
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
