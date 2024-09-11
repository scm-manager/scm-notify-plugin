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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public interface ContentBuilder
{

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param configuration
   * @param changesets
   *  @return
   *
   * @throws IOException
   */
  Object createModel(Repository repository,
                               NotifyRepositoryConfiguration configuration, Changeset... changesets)
    throws IOException;

  /**
   * Method description
   *
   *
   * @param repository
   * @param changesets
   * @return
   */
  String createSubject(Repository repository, Changeset... changesets);
}
