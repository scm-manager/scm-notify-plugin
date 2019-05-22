/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
