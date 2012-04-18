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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
   * @param repository
   * @param contacts
   */
  public DefaultNotifyHandler(ContentBuilder contentBuilder,
                              NotifyConfiguration configuration,
                              Repository repository, Set<String> contacts)
  {
    this.contentBuilder = contentBuilder;
    this.configuration = configuration;
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
        Session session = NotifyUtil.createSession(configuration);
        Message message = createMessage(session, changesets);

        Transport.send(message);
      }
      catch (MessagingException ex)
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
   * @throws MessagingException
   */
  private Message createMessage(Session session,
                                Collection<Changeset> changesets)
          throws MessagingException
  {
    MimeMessage msg = new MimeMessage(session);

    msg.setFrom(new InternetAddress(configuration.getFrom()));
    msg.setRecipients(RecipientType.BCC, createRecipients());
    msg.setSubject(createSubject());

    Content content = contentBuilder.createContent(repository, changesets);

    msg.setContent(content.getBody(), content.getMimeType());

    return msg;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws AddressException
   */
  private Address[] createRecipients() throws AddressException
  {
    String[] contactArray = contacts.toArray(new String[0]);
    Address[] addresses = new Address[contactArray.length];

    for (int i = 0; i < contactArray.length; i++)
    {
      addresses[i] = new InternetAddress(contactArray[i]);
    }

    return addresses;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createSubject()
  {
    StringBuilder content = new StringBuilder();
    String prefix = configuration.getSubjectPrefix();

    if (Util.isNotEmpty(prefix))
    {
      content.append(prefix).append(" ");
    }

    content.append(contentBuilder.createSubject(repository));

    return content.toString();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private NotifyConfiguration configuration;

  /** Field description */
  private Set<String> contacts;

  /** Field description */
  private ContentBuilder contentBuilder;

  /** Field description */
  private Repository repository;
}
