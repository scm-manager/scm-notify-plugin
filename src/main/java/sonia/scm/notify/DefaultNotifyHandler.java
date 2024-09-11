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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultNotifyHandler implements NotifyHandler {

  /**
   * the logger for DefaultNotifyHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultNotifyHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param contentBuilder
   * @param mailService
   * @param repository
   * @param contacts
   * @param notifyConfiguration
   */
  public DefaultNotifyHandler(ContentBuilder contentBuilder, MailService mailService, Repository repository, Set<String> contacts,
                              NotifyRepositoryConfiguration notifyConfiguration) {
    this.contentBuilder = contentBuilder;
    this.mailService = mailService;
    this.repository = repository;
    this.contacts = contacts;
    this.notifyConfiguration = notifyConfiguration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param changesets
   */
  @Override
  public void send(Iterable<Changeset> changesets) {
    if (Util.isNotEmpty(contacts)) {
      logger.debug("try to send notification to {}", contacts);
      try {
        List<Changeset> list = Lists.newArrayList(changesets);

        if (notifyConfiguration.isEmailPerPush()) {
          sendMessage(list.toArray(new Changeset[0]));
        } else for (Changeset c : changesets) {
          sendMessage(c);
        }
      } catch (Exception ex) {
        logger.error("could not send notification", ex);
      }
    }
    logger.debug("no contacts found");
  }

  private void sendMessage(Changeset... changesets) throws MailSendBatchException, IOException {
    MailService.EnvelopeBuilder envelopeBuilder = mailService.emailTemplateBuilder();

    if (notifyConfiguration.isUseAuthorAsFromAddress() && changesets.length > 0) {
      envelopeBuilder.fromCurrentUser();
    }
    contacts.forEach(envelopeBuilder::toAddress);

    envelopeBuilder.withSubject(contentBuilder.createSubject(repository, changesets))
      .withTemplate("/sonia/scm/notify/template/content.mustache", MailTemplateType.MARKDOWN_HTML)
      .andModel(contentBuilder.createModel(repository, notifyConfiguration, changesets))
      .send();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<String> contacts;

  private final NotifyRepositoryConfiguration notifyConfiguration;

  /** Field description */
  private ContentBuilder contentBuilder;

  /** Field description */
  private MailService mailService;

  /** Field description */
  private Repository repository;
}
