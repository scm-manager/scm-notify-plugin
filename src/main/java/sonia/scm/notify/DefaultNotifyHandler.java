/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.notify;

//~--- non-JDK imports --------------------------------------------------------

import org.codemonkey.simplejavamail.Email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.mail.api.MailService;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Set;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultNotifyHandler implements NotifyHandler
{

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
   * @param configuration
   * @param mailService
   * @param repository
   * @param contacts
   */
  public DefaultNotifyHandler(ContentBuilder contentBuilder,
    MailService mailService, Repository repository, Set<String> contacts)
  {
    this.contentBuilder = contentBuilder;
    this.mailService = mailService;
    this.repository = repository;
    this.contacts = contacts;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param changesets
   */
  @Override
  public void send(Collection<Changeset> changesets)
  {
    if (Util.isNotEmpty(contacts))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("try to send notification to {}", contacts);
      }

      try
      {
        Email mail = createMessage(changesets);

        mailService.send(mail);
      }
      catch (Exception ex)
      {
        logger.error("could not send notification", ex);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no contacts found");
    }
  }

  /**
   * Method description
   *
   *
   * @param session
   * @param changesets
   *
   * @return
   *
   *
   * @throws IOException
   * @throws MessagingException
   */
  private Email createMessage(Collection<Changeset> changesets)
    throws MessagingException, IOException
  {
    Email msg = new Email();

    for (String c : contacts)
    {
      msg.addRecipient(null, c, RecipientType.BCC);
    }

    msg.setSubject(contentBuilder.createSubject(repository));

    Content content = contentBuilder.createContent(repository, changesets);

    if (content.isHtml())
    {
      msg.setTextHTML(content.getContent());
    }
    else
    {
      msg.setText(content.getContent());
    }

    return msg;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<String> contacts;

  /** Field description */
  private ContentBuilder contentBuilder;

  /** Field description */
  private MailService mailService;

  /** Field description */
  private Repository repository;
}
