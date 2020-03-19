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
