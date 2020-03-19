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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractContentBuilder implements ContentBuilder
{

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

  //~--- methods --------------------------------------------------------------

  /**
   * the logger for AbstractContentBuilder
   */
  private static final Logger logger = LoggerFactory.getLogger(
    AbstractContentBuilder.class);
  
  /**
   * Method description
   *
   * @param repository
   * @param changesets
   * @return
   */
  @Override
  public String createSubject(Repository repository, Changeset... changesets)
  {
    final StringBuilder branchString = new StringBuilder();
    final Set<String> branches = new HashSet<String>();

    final StringBuilder idString = new StringBuilder();

    boolean branchesElided = false;
    for (Changeset c : changesets) {
      branchesElided = updateBranchString(branchString, c, branchesElided, branches);

      updateChangesetIdString(idString, c);
    }

    String result = MessageFormat.format(PATTERN_SUBJECT,
        repository.getName(),
        branchString.toString(),
        idString.toString());
    if (result.length() > MAX_SUBJECT_LENGTH) {
      logger.trace("notification subject exceeded maximum length");
      // Exceeded the max length, find the last ID separator before that length.
      int lastSep = result.lastIndexOf(SEP, MAX_SUBJECT_LENGTH - 3);
      // Chop & elide the rest of the subject
      result = result.substring(0, lastSep) + "...";
    }

    // Will look something like:
    //  [repo][branch]  42,63,73
    //  [repo][branch1,branch2]  42,63,73
    //  [repo][branch1,branch2,branch3...]  42,63,73,82...

    return result;
  }


  private void updateChangesetIdString(StringBuilder idString, Changeset c) {
    if (idString.length() > 0) { idString.append(SEP); }
    idString.append( shortenId(c.getId()) );
    if (isMerge(c)) {
      logger.trace("mark changeset {} as merge", c.getId());
      idString.append(" (merge)");
    }
  }


  private boolean updateBranchString(StringBuilder branchString, Changeset c, boolean branchesElided,
      Set<String> branches) {

    List<String> cBranches = c.getBranches();
    if (cBranches.isEmpty()) {
      logger.trace("empty list of branches. Mercurial default branch?");
      cBranches.add( getDefaultBranchName() ); // No branch?  That means "default"
    }

    // Iterate through each branch in the changeset.
    for (String branch : cBranches) {
      if (branches.add( branch )) {
        // We haven't seen this one before, so add it to the String.

        if (branches.size() <= MAX_BRANCHES_IN_SUBJECT) {
          // Still within the maximum, add it (conditionally a separator)
          if (branchString.length() > 0) { branchString.append(SEP); }
          branchString.append(branch);
        }
        else if (!branchesElided) {
          // Oops, over the maximum.  Add some indicator ...
          logger.trace("exceeded maximum branch length");
          branchString.append("...");
          branchesElided = true;
        }
      }
    }

    return branchesElided;
  }


  public static boolean isMerge(Changeset changeset){
    return changeset.getParents().size() > 1;
  }


  public static String shortenId(String id) {
    if (id.length() > 8) {
      id = id.substring(0,8);
    }
    return id;
  }


  public static String getDefaultBranchName() {
    return "default";// TODO: Mercurial-specific - is there a better way ?
  }
}
