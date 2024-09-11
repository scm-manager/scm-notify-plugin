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


import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Sebastian Sdorra
 */
public class ChangesetTemplateWrapper
{

  private final Modifications modifications;

  /**
   * Constructs ...
   *
   * @param changeset
   * @param link
   */
  public ChangesetTemplateWrapper(Changeset changeset, String link)
  {
    this(changeset, link, null, null);
  }

  /**
   * Constructs ...
   *
   * @param changeset
   * @param link
   * @param diff
   * @param modifications
   */
  public ChangesetTemplateWrapper(Changeset changeset, String link, String diff, Modifications modifications)
  {
    this.modifications = modifications;
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
    return modifications;
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
