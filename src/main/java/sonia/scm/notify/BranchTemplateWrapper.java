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


import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchTemplateWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param changesets
   */
  public BranchTemplateWrapper(String name,
    List<ChangesetTemplateWrapper> changesets)
  {
    this.name = name;
    this.changesets = changesets;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<ChangesetTemplateWrapper> getChangesets()
  {
    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<ChangesetTemplateWrapper> changesets;

  /** Field description */
  private String name;
}
