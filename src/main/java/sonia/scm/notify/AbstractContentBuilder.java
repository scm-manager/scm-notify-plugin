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

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.url.UrlUtil;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    id = UrlUtil.fixRevision( id );
    if (id.length() > 8) {
      id = id.substring(0,8);
    }
    return id;
  }


  public static String getDefaultBranchName() {
    return "default";// TODO: Mercurial-specific - is there a better way ?
  }
}
