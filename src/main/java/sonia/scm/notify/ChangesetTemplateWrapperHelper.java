/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;



/**
 * @author Sebastian Sdorra
 */
public class ChangesetTemplateWrapperHelper implements Closeable
{

  private static final String LINE_SEPARATOR =
      System.getProperty("line.separator");

  private static final String MSG_REACHEDDIFFLIMIT =
      " ** Diff limit reached (max: %d lines) **";

  private static final Logger logger =
      LoggerFactory.getLogger(ChangesetTemplateWrapperHelper.class);
  public static final String SCM_CHANGESET_URL_PATTERN = "{0}/repo/{1}/{2}/changeset/{3}";

  private int diffLineCount = 0;
  private int maxDiffLines;
  private String reachedDiffLimitMessage;
  private Repository repository;
  private final RepositoryService repositoryService;
  private final String baseUrl;

  public ChangesetTemplateWrapperHelper(ScmConfiguration configuration,
                                        RepositoryServiceFactory repositoryServiceFactory,
                                        NotifyRepositoryConfiguration notifyConfiguration, Repository repository) {

    baseUrl = configuration.getBaseUrl();

    maxDiffLines = notifyConfiguration.getMaxDiffLines();
    repositoryService = repositoryServiceFactory.create(repository);

    this.repository = repository;
    reachedDiffLimitMessage = String.format(MSG_REACHEDDIFFLIMIT, maxDiffLines);
  }

  @Override
  public void close() throws IOException
  {
    Closeables.close(repositoryService, false);
  }

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

  private boolean appendDiffLines()
  {
    return (maxDiffLines == -1) || (diffLineCount < maxDiffLines);
  }

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

  private String createLink(Repository repository, Changeset changeset)
  {
    return MessageFormat.format(SCM_CHANGESET_URL_PATTERN, baseUrl, repository.getNamespace(), repository.getName(), changeset.getId());
  }

  private List<ChangesetTemplateWrapper> wrap(Changeset[] changesets)
  {
    List<ChangesetTemplateWrapper> wrapperList = Lists.newArrayList();

    for (Changeset c : changesets) {
      wrapperList.add(wrap(c));
    }

    return wrapperList;
  }

  private ChangesetTemplateWrapper wrap(Changeset changeset)
  {
    Modifications modifications = createModifications(changeset);
    String link = createLink(repository, changeset);
    String diff = createDiffOrMessage(changeset);
    return new ChangesetTemplateWrapper(changeset, link, diff, modifications);
  }

  @Nullable
  private String createDiffOrMessage(Changeset changeset) {
    if (appendDiffLines()) {
      return createDiff(changeset);
    } else if (maxDiffLines != 0) {
      return reachedDiffLimitMessage;
    } else {
      return null;
    }
  }

  @Nullable
  private Modifications createModifications(Changeset changeset) {
    Modifications modifications;
    try {
      modifications = repositoryService.getModificationsCommand()
        .revision(changeset.getId())
        .getModifications();
    } catch (IOException e) {
      modifications = null;
    }
    return modifications;
  }

  private static class ChangesetTemplateWrapperOrder
      extends Ordering<ChangesetTemplateWrapper>
  {

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
}
