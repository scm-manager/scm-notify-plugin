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
import com.google.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultContentBuilder extends AbstractContentBuilder
{

   /** Field description */
  private static final String TPYE_SVN = "svn";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param repositoryServiceFactory
   */
  @Inject
  public DefaultContentBuilder(
    ScmConfiguration configuration,
    RepositoryServiceFactory repositoryServiceFactory)
  {
    this.configuration = configuration;
    this.repositoryServiceFactory = repositoryServiceFactory;
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
  public Object createModel(Repository repository,
                               NotifyRepositoryConfiguration configuration, Changeset... changesets)
    throws IOException
  {
    List<BranchTemplateWrapper> branches;

    try(ChangesetTemplateWrapperHelper helper = new ChangesetTemplateWrapperHelper(this.configuration,repositoryServiceFactory, configuration, repository))
    {
      branches = helper.wrapAndSortByBranch(changesets);
    }

    Map<String, Object> env = Maps.newHashMap();

    env.put("title", createSubject(repository, changesets));
    env.put("repository", repository);
    env.put("branches", branches);
    env.put("supportNamedBranches", isNamedBranchesSupported(repository));

    return env;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private boolean isNamedBranchesSupported(Repository repository)
  {

    return !TPYE_SVN.equalsIgnoreCase(repository.getType());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryServiceFactory repositoryServiceFactory;

  /** Field description */
  private ScmConfiguration configuration;

}
