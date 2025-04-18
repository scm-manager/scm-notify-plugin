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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractContentBuilder implements ContentBuilder {

  /**
   * [repository][branches...] changeset IDs...
   */
  public static final String PATTERN_SUBJECT = "[{0}][{1}] {2}";

  /**
   * Limit the size of the subject.
   */
  public static final int MAX_BRANCHES_IN_SUBJECT = 3;
  public static final int MAX_SUBJECT_LENGTH = 100;

  private static final char SEP = ',';

  private static final Logger logger = LoggerFactory.getLogger(AbstractContentBuilder.class);

  @Override
  public String createSubject(Repository repository, Changeset... changesets) {
    final StringBuilder branchString = new StringBuilder();
    final Set<String> branches = new HashSet<>();

    final StringBuilder idString = new StringBuilder();

    boolean branchesElided = false;
    for (Changeset c : changesets) {
      branchesElided = updateBranchString(branchString, c, branchesElided, branches);

      updateChangesetIdString(idString, c);
    }

    String result = MessageFormat.format(PATTERN_SUBJECT,
      repository.getName(),
      branchString.toString(),
      idString.toString()
    );

    if (result.length() > MAX_SUBJECT_LENGTH) {
      logger.trace("notification subject exceeded maximum length");
      // Exceeded the max length, find the last ID separator before that length.
      int lastSep = result.lastIndexOf(SEP, MAX_SUBJECT_LENGTH - 3);
      // Chop & elide the rest of the subject
      if (lastSep != -1) {
        result = result.substring(0, lastSep) + "...";
      } else {
        result = result.substring(0, MAX_SUBJECT_LENGTH - 3) + "...";
      }
    }

    // Will look something like:
    //  [repo][branch]  42,63,73
    //  [repo][branch1,branch2]  42,63,73
    //  [repo][branch1,branch2,branch3...]  42,63,73,82...

    return result;
  }


  private void updateChangesetIdString(StringBuilder idString, Changeset c) {
    if (idString.length() > 0) {
      idString.append(SEP);
    }
    idString.append(shortenId(c.getId()));
    if (isMerge(c)) {
      logger.trace("mark changeset {} as merge", c.getId());
      idString.append(" (merge)");
    }
  }


  private boolean updateBranchString(StringBuilder branchString, Changeset c, boolean branchesElided, Set<String> branches) {
    List<String> cBranches = c.getBranches();
    if (cBranches.isEmpty()) {
      logger.trace("empty list of branches. Mercurial default branch?");
      cBranches.add(getDefaultBranchName()); // No branch?  That means "default"
    }

    // Iterate through each branch in the changeset.
    for (String branch : cBranches) {
      if (branches.add(branch)) {
        // We haven't seen this one before, so add it to the String.

        if (branches.size() <= MAX_BRANCHES_IN_SUBJECT) {
          // Still within the maximum, add it (conditionally a separator)
          if (branchString.length() > 0) {
            branchString.append(SEP);
          }
          branchString.append(branch);
        } else if (!branchesElided) {
          // Oops, over the maximum.  Add some indicator ...
          logger.trace("exceeded maximum branch length");
          branchString.append("...");
          branchesElided = true;
        }
      }
    }

    return branchesElided;
  }


  public static boolean isMerge(Changeset changeset) {
    return changeset.getParents().size() > 1;
  }


  public static String shortenId(String id) {
    if (id.length() > 8) {
      id = id.substring(0, 8);
    }
    return id;
  }


  public static String getDefaultBranchName() {
    return "default";// TODO: Mercurial-specific - is there a better way ?
  }
}
