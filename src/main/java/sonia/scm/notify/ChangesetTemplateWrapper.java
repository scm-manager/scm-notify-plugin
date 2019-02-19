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
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class ChangesetTemplateWrapper
{

  private final RepositoryService service;

  /**
   * Constructs ...
   *
   *
   * @param changeset
   * @param link
   */
  public ChangesetTemplateWrapper(RepositoryService service, Changeset changeset, String link)
  {
    this(service, changeset, link, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param changeset
   * @param link
   * @param diff
   */
  public ChangesetTemplateWrapper(RepositoryService service, Changeset changeset, String link, String diff)
  {
    this.service = service;
    this.changeset = changeset;
    this.link = link;
    this.diff = diff;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Person getAuthor()
  {
    return changeset.getAuthor();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getBranches()
  {
    return changeset.getBranches();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBranchesAsString()
  {
    StringBuilder ret = new StringBuilder();

    for (String branch : changeset.getBranches())
    {
      if (ret.length() > 0)
      {
        ret.append(",");
      }

      ret.append(branch);
    }

    if (ret.length() == 0)
    {
      ret.append(AbstractContentBuilder.getDefaultBranchName());
    }

    return ret.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Date getDate()
  {
    Date date = null;
    Long time = changeset.getDate();

    if (time != null)
    {
      date = new Date(time);
    }

    return date;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return changeset.getDescription();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDiff()
  {
    return diff;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return changeset.getId();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getLink()
  {
    return link;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Modifications getModifications()
  {
    try {
      return service.getModificationsCommand()
        .revision(changeset.getId())
        .getModifications();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Map<String, String> getProperties()
  {
    return changeset.getProperties();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getShortId()
  {
    String id = changeset.getId();

    if (id.length() > 8)
    {
      id = id.substring(0, 8);
    }

    return id;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getTags()
  {
    return changeset.getTags();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Changeset changeset;

  /** Field description */
  private String diff;

  /** Field description */
  private String link;
}
