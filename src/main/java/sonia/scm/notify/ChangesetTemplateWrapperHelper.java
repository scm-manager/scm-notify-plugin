/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.notify;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.EscapeUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.url.RepositoryUrlProvider;
import sonia.scm.url.UrlProviderFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class ChangesetTemplateWrapperHelper implements Closeable
{

  /** Field description */
  private static final String LINE_SEPARATOR =
    System.getProperty("line.separator");

  /** Field description */
  private static final String MSG_REACHEDDIFFLIMIT =
    " ** Diff limit reached (max: %d lines) **";

  /**
   * the logger for ChangesetTemplateWrapperHelper
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ChangesetTemplateWrapperHelper.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param repositoryServiceFactory
   * @param notifyConfiguration
   * @param repository
   */
  public ChangesetTemplateWrapperHelper(ScmConfiguration configuration,
    RepositoryServiceFactory repositoryServiceFactory,
    NotifyRepositoryConfiguration notifyConfiguration, Repository repository)
  {
    urlProvider =
      UrlProviderFactory.createUrlProvider(configuration.getBaseUrl(),
        UrlProviderFactory.TYPE_WUI).getRepositoryUrlProvider();

    maxDiffLines = notifyConfiguration.maxDiffLines();

    if (appendDiffLines())
    {
      repositoryService = repositoryServiceFactory.create(repository);
    }

    this.repository = repository;
    reachedDiffLimitMessage = String.format(MSG_REACHEDDIFFLIMIT, maxDiffLines);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    Closeables.close(repositoryService, false);
  }

  /**
   * Method description
   *
   *
   * @param changesets
   *
   * @return
   */
  public List<ChangesetTemplateWrapper> wrap(Changeset[] changesets)
  {
    Builder<ChangesetTemplateWrapper> builder = ImmutableList.builder();

    for (Changeset c : changesets)
    {
      builder.add(wrap(c));
    }

    return builder.build();
  }

  /**
   * Method description
   *
   *
   * @param changeset
   *
   * @return
   */
  public ChangesetTemplateWrapper wrap(Changeset changeset)
  {
    String link = createLink(urlProvider, repository, changeset);
    String diff = null;

    if (appendDiffLines())
    {
      diff = createDiff(changeset);
    }
    else if (maxDiffLines != 0)
    {
      diff = reachedDiffLimitMessage;
    }

    return new ChangesetTemplateWrapper(changeset, link, diff);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private boolean appendDiffLines()
  {
    return (maxDiffLines == -1) || (diffLineCount < maxDiffLines);
  }

  /**
   * Method description
   *
   *
   * @param changeset
   *
   * @return
   */
  private String createDiff(Changeset changeset)
  {
    String diff = null;

    try
    {
      diff = repositoryService.getDiffCommand().setRevision(
        changeset.getId()).getContent();
      diff = EscapeUtil.escape(Strings.nullToEmpty(diff));

      String[] diffLines = diff.split(LINE_SEPARATOR);

      if (diffLineCount + diffLines.length > maxDiffLines)
      {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; diffLineCount++ < maxDiffLines; i++)
        {
          buffer.append(diffLines[i]).append(LINE_SEPARATOR);
        }

        buffer.append(reachedDiffLimitMessage).append(LINE_SEPARATOR);
        diff = buffer.toString();
      }
      else
      {
        diffLineCount += diffLines.length;
      }

    }
    catch (Exception ex)
    {
      logger.error("could not append diff", ex);
    }

    return diff;
  }

  /**
   * Method description
   *
   *
   * @param urlProvider
   * @param repository
   * @param c
   *
   * @return
   */
  private String createLink(RepositoryUrlProvider urlProvider,
    Repository repository, Changeset c)
  {
    return urlProvider.getChangesetUrl(repository.getId(), c.getId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private int diffLineCount = 0;

  /** Field description */
  private int maxDiffLines;

  /** Field description */
  private String reachedDiffLimitMessage;

  /** Field description */
  private Repository repository;

  /** Field description */
  private RepositoryService repositoryService;

  /** Field description */
  private RepositoryUrlProvider urlProvider;
}
