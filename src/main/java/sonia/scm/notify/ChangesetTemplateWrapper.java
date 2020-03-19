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


import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


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
