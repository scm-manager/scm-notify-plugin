/**
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


import com.google.common.base.Strings;
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
import java.text.MessageFormat;
import java.util.List;



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
  public static final String SCM_CHANGESET_URL_PATTERN = "{0}/repo/{1}/{2}/changeset/{3}";
  private final String baseUrl;

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

    baseUrl = configuration.getBaseUrl();

    maxDiffLines = notifyConfiguration.getMaxDiffLines();
    repositoryService = repositoryServiceFactory.create(repository);

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
      diff = Strings.nullToEmpty(diff);

      logger.trace("diff:{}", diff);

      String[] diffLines = diff.split("\r\n?|\n");
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
   * @param repository
   * @param changeset
   * @return
   */
  private String createLink(Repository repository, Changeset changeset)
  {
    return MessageFormat.format(SCM_CHANGESET_URL_PATTERN, baseUrl, repository.getNamespace(), repository.getName(), changeset.getId());
  }

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
    String link = createLink(repository, changeset);
    String diff = null;

    if (appendDiffLines()) {
      diff = createDiff(changeset);
    } else if (maxDiffLines != 0) {
      diff = reachedDiffLimitMessage;
    }

    return new ChangesetTemplateWrapper(repositoryService, changeset, link, diff);
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
  private String reachedDiffLimitMessage;

  /** Field description */
  private Repository repository;

  /** Field description */
  private final RepositoryService repositoryService;

}
