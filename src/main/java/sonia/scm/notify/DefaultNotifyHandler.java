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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.codemonkey.simplejavamail.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailService;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.security.Role;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

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
   * @param mailService
   * @param repository
   * @param contacts
   * @param notifyConfiguration
   */
  public DefaultNotifyHandler(ContentBuilder contentBuilder, MailService mailService, Repository repository, Set<String> contacts,
      NotifyRepositoryConfiguration notifyConfiguration)
  {
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
        Changeset[] changesetArray = changesets.toArray(new Changeset[changesets.size()]);

        if (notifyConfiguration.isEmailPerPush()) {
          Email mail = createMessage(changesetArray);
          if (null != mail) {
            sendMessage(mail);
          }
        }
        else for (Changeset c : changesets) {
          Email mail = createMessage(c);
          if (null != mail) {
            sendMessage(mail);
          }
        }
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
  
  private void sendMessage( Email email ) throws MailSendBatchException
  {
    List<Email> emails = Lists.newArrayList();
    NotifyEmail notification = new NotifyEmail(email);
    for ( String c : contacts )
    {
      NotifyEmail ne = notification.copy();
      ne.addRecipient(null, c, RecipientType.TO);
      emails.add(ne);
    }
    
    mailService.send(emails);
  }

  /**
   * Method description
   *
   *
   * @param changesets
   *
   * @return
   *
   *
   * @throws IOException
   * @throws MessagingException
   */
  private Email createMessage(Changeset... changesets)
    throws MessagingException, IOException
  { 
    Email msg = new Email();

    msg.setSubject(contentBuilder.createSubject(repository, changesets));

    if (notifyConfiguration.isUseAuthorAsFromAddress() && changesets.length > 0)
    {
      // use mail address of current user
      Subject subject = SecurityUtils.getSubject();
      subject.checkRole( Role.USER );
      User user = subject.getPrincipals().oneByType(User.class);

      String mail = user.getMail();
      
      if (!Strings.isNullOrEmpty(mail))
      {
        String displayName = user.getDisplayName();
        if ( Strings.isNullOrEmpty(displayName) )
        {
          displayName = user.getName();
        }
        logger.debug("user \"{} <{}>\" as from address", displayName, mail);
        msg.setFromAddress(displayName, mail);
      } 
      else 
      {
        logger.warn("user {} has no mail address, use default address", user.getName());
      }
    }

    Content content = contentBuilder.createContent(repository, notifyConfiguration, changesets);

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

  private final NotifyRepositoryConfiguration notifyConfiguration;

  /** Field description */
  private ContentBuilder contentBuilder;

  /** Field description */
  private MailService mailService;

  /** Field description */
  private Repository repository;
}
