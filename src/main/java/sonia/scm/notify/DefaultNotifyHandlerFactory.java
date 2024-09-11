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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import sonia.scm.mail.api.MailService;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultNotifyHandlerFactory implements NotifyHandlerFactory
{

  /**
   * Constructs ...
   *
   *
   * @param contentBuilderProvider
   */
  @Inject
  public DefaultNotifyHandlerFactory(Provider<ContentBuilder> contentBuilderProvider)
  {
    this.contentBuilderProvider = contentBuilderProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param mailService
   * @param repositoryConfiguration
   * @param repository
   *
   * @return
   */
  @Override
  public NotifyHandler createHandler(MailService mailService,
    NotifyRepositoryConfiguration repositoryConfiguration,
    Repository repository)
  {
    Set<String> contacts = new HashSet<>();

    if (repositoryConfiguration.isSendToRepositoryContact())
    {
      String repositoryContact = repository.getContact();

      if (Util.isNotEmpty(repositoryContact))
      {
        contacts.add(repositoryContact);
      }
    }

    contacts.addAll(repositoryConfiguration.getContactList());

    return new DefaultNotifyHandler(contentBuilderProvider.get(), mailService, repository,
      contacts, repositoryConfiguration);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<ContentBuilder> contentBuilderProvider;
}
