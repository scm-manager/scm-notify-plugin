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

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Closeables;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------


/**
 * @author Sebastian Sdorra
 */
public class ChangesetTemplateWrapperHelper implements Closeable
{

  /**
   * Field description
   */
  private static final String LINE_SEPARATOR =
      System.getProperty("line.separator");

  /**
   * Field description
   */
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
   * @param configuration
   * @param repositoryServiceFactory
   * @param notifyConfiguration
   * @param repository
   */
  public ChangesetTemplateWrapperHelper(ScmConfiguration configuration,
                                        RepositoryServiceFactory repositoryServiceFactory,
                                        NotifyRepositoryConfiguration notifyConfiguration, Repository repository)
  {
    // fixme
    //    urlProvider =
//        UrlProviderFactory.createUrlProvider(configuration.getBaseUrl(),
//            UrlProviderFactory.TYPE_WUI).getRepositoryUrlProvider();

    maxDiffLines = notifyConfiguration.getMaxDiffLines();
    usePrettyDiff = notifyConfiguration.isUsePrettyDiff();

    if (appendDiffLines()) {
      repositoryService = repositoryServiceFactory.create(repository);
    }

    this.repository = repository;
    reachedDiffLimitMessage = String.format(MSG_REACHEDDIFFLIMIT, maxDiffLines);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
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
   * @param changesets
   * @return
   */
  public List<BranchTemplateWrapper> wrapAndSortByBranch(Changeset[] changesets)
  {
    Builder<BranchTemplateWrapper> builder = ImmutableList.builder();

    List<ChangesetTemplateWrapper> wrapped = wrap(changesets);

    Ordering<ChangesetTemplateWrapper> order =
        new ChangesetTemplateWrapperOrder();

    wrapped = order.immutableSortedCopy(wrapped);

    String branch = null;
    Builder<ChangesetTemplateWrapper> changesetBuilder =
        ImmutableList.builder();

    for (ChangesetTemplateWrapper c : wrapped) {
      if (branch == null) {
        branch = c.getBranchesAsString();
        changesetBuilder.add(c);
      } else if (branch.equals(c.getBranchesAsString())) {
        changesetBuilder.add(c);
      } else {
        builder.add(new BranchTemplateWrapper(branch,
            changesetBuilder.build()));
        changesetBuilder = ImmutableList.builder();
        branch = c.getBranchesAsString();
      }
    }

    wrapped = changesetBuilder.build();

    if (!wrapped.isEmpty()) {
      builder.add(new BranchTemplateWrapper(branch, changesetBuilder.build()));
    }

    return builder.build();
  }

  /**
   * Method description
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
   * @param changeset
   * @return
   */
  private String createDiff(Changeset changeset)
  {
    String diff = null;

    try {
      diff = repositoryService.getDiffCommand().setFormat(
          DiffFormat.NATIVE).setRevision(changeset.getId()).getContent();
      // fixme
      //      diff = EscapeUtil.escape(Strings.nullToEmpty(diff));

      logger.trace("diff:{}", diff);

      String[] diffLines = diff.split("\r\n?|\n");

      if (usePrettyDiff)
      {
          diffLines = DiffWrapper.wrap(diffLines);
      }

      diff = StringUtils.join(diffLines, LINE_SEPARATOR);

      if (diffLineCount + diffLines.length > maxDiffLines) {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; diffLineCount++ < maxDiffLines; i++) {
          buffer.append(diffLines[i]).append(LINE_SEPARATOR);
        }

        buffer.append(reachedDiffLimitMessage).append(LINE_SEPARATOR);
        diff = buffer.toString();
      } else {
        diffLineCount += diffLines.length;
      }

    } catch (Exception ex) {
      logger.error("could not append diff", ex);
    }

    return diff;
  }

  /**
   * Method description
   *
   * @param urlProvider
   * @param repository
   * @param c
   * @return
   */
  // fixme
//  private String createLink(RepositoryUrlProvider urlProvider,
//                            Repository repository, Changeset c)
//  {
//    return urlProvider.getChangesetUrl(repository.getId(), c.getId());
//  }

  /**
   * Method description
   *
   * @param changesets
   * @return
   */
  private List<ChangesetTemplateWrapper> wrap(Changeset[] changesets)
  {
    List<ChangesetTemplateWrapper> wrapperList = Lists.newArrayList();

    for (Changeset c : changesets) {
      wrapperList.add(wrap(c));
    }

    return wrapperList;
  }

  /**
   * Method description
   *
   * @param changeset
   * @return
   */
  private ChangesetTemplateWrapper wrap(Changeset changeset)
  {
    return null;
    // fixme
//    String link = createLink(urlProvider, repository, changeset);
//    String diff = null;
//
//    if (appendDiffLines()) {
//      diff = createDiff(changeset);
//    } else if (maxDiffLines != 0) {
//      diff = reachedDiffLimitMessage;
//    }
//
//    return new ChangesetTemplateWrapper(changeset, link, diff);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 13/08/02
   */
  private static class ChangesetTemplateWrapperOrder
      extends Ordering<ChangesetTemplateWrapper>
  {

    /**
     * Method description
     *
     * @param left
     * @param right
     * @return
     */
    @Override
    public int compare(ChangesetTemplateWrapper left,
                       ChangesetTemplateWrapper right)
    {
      //J-
      return ComparisonChain
          .start()
          .compare(left.getBranchesAsString(), right.getBranchesAsString())
          .compare(left.getDate(), right.getDate())
          .result();
      //J+
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private int diffLineCount = 0;

  /** Field description */
  private int maxDiffLines;

  /** Field description */
  private boolean usePrettyDiff;

  /** Field description */
  private String reachedDiffLimitMessage;

  /** Field description */
  private Repository repository;

  /** Field description */
  private RepositoryService repositoryService;

  /** Field description */
//  private RepositoryUrlProvider urlProvider;
}
