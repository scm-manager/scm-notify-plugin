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
