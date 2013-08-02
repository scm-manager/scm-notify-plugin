/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.notify;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import freemarker.cache.ClassTemplateLoader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class FreemarkerContentBuilder extends AbstractContentBuilder
{

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String PATH_BASE = "/sonia/scm/notify/template/";

  /** Field description */
  public static final String PATH_TEMPLATE = "content.ftl";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param configuration
   * @param repositoryServiceFactory
   */
  @Inject
  public FreemarkerContentBuilder(SCMContextProvider context,
    ScmConfiguration configuration,
    RepositoryServiceFactory repositoryServiceFactory)
  {
    this.configuration = configuration;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.templateConfiguration = new Configuration();
    this.templateConfiguration.setTemplateLoader(
      new ClassTemplateLoader(FreemarkerContentBuilder.class, PATH_BASE));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param configuration
   * @param changesets
   *  @return
   *
   * @throws IOException
   */
  @Override
  public Content createContent(Repository repository,
    NotifyRepositoryConfiguration configuration, Changeset... changesets)
    throws IOException
  {
    List<ChangesetTemplateWrapper> wrapperList = null;
    ChangesetTemplateWrapperHelper helper = null;

    try
    {
      helper = new ChangesetTemplateWrapperHelper(this.configuration,
        repositoryServiceFactory, configuration, repository);
      wrapperList = helper.wrap(changesets);
    }
    finally
    {
      Closeables.close(helper, true);
    }

    Map<String, Object> env = Maps.newHashMap();

    env.put("title", createSubject(repository, changesets));
    env.put("repository", repository);
    env.put("changesets", wrapperList);

    Template tpl = templateConfiguration.getTemplate(PATH_TEMPLATE, ENCODING);
    StringWriter writer = new StringWriter();

    try
    {
      tpl.process(env, writer);
    }
    catch (TemplateException ex)
    {
      throw new ContentBuilderException("could not create content", ex);
    }

    return new Content(writer.toString(), true);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryServiceFactory repositoryServiceFactory;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private Configuration templateConfiguration;
}
