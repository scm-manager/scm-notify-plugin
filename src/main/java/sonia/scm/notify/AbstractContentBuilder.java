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

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;
import java.util.HashSet;
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
   * Method description
   *
   * @param repository
   * @param changesets
   * @return
   */
  @Override
  public String createSubject(Repository repository, Changeset... changesets)
  {
    StringBuilder branchString = new StringBuilder();
    StringBuilder idString = new StringBuilder();

    boolean branchesElided = false;
    Set<String> branches = new HashSet<String>();
    for (Changeset c : changesets) {
      for (String branch : c.getBranches()) {
        if (branches.add( branch )) {
          if (branches.size() <= MAX_BRANCHES_IN_SUBJECT) {
            // Restrict the # of branches displayed in the subject.
            if (branchString.length() > 0) { branchString.append(SEP); }
            branchString.append(branch);
          }
          else if (!branchesElided) {
            branchString.append("...");
          }
        }
      }

      if (idString.length() > 0) { idString.append(SEP); }
      idString.append(c.getId());
    }

    String result = MessageFormat.format(PATTERN_SUBJECT,  repository.getName(),
        branchString.toString(), idString.toString());
    if (result.length() > MAX_SUBJECT_LENGTH) {
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
}
