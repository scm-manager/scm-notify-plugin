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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.codemonkey.simplejavamail.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.util.Util;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

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
