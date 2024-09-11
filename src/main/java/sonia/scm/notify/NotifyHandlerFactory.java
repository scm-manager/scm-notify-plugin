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

import sonia.scm.mail.api.MailService;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Repository;

/**
 *
 * @author Sebastian Sdorra
 */
public interface NotifyHandlerFactory
{

  /**
   * Method description
   *
   *
   * @param configuration
   * @param repositoryConfiguration
   * @param repository
   *
   * @return
   */
  public NotifyHandler createHandler(
          MailService mailService,
          NotifyRepositoryConfiguration repositoryConfiguration,
          Repository repository);
}
