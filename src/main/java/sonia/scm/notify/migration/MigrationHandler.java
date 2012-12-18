/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.notify.migration;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.codemonkey.simplejavamail.TransportStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailService;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class MigrationHandler
{

  /** Field description */
  private static final String STORE = "notify";

  /**
   * the logger for MigrationHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MigrationHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   * @param mailService
   * @param mailContext
   */
  @Inject
  public MigrationHandler(StoreFactory storeFactory, MailService mailService,
    MailContext mailContext)
  {
    this.configurationStore = storeFactory.getStore(NotifyConfiguration.class,
      STORE);
    this.mailService = mailService;
    this.mailContext = mailContext;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void migrate()
  {
    if (!mailService.isConfigured())
    {
      NotifyConfiguration configuration = configurationStore.get();

      if (configuration != null)
      {
        if (!configuration.isMigrated())
        {
          migrate(configuration);
        }
        else if (logger.isDebugEnabled())
        {
          logger.debug(
            "notify configuration is already migrated, skip migration");
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("no notify configuration found, skip migration");
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("mail service is already configured, skip migration");
    }

  }

  /**
   * Method description
   *
   *
   * @param cs
   *
   * @return
   */
  private TransportStrategy convert(ConnectionSecurity cs)
  {
    TransportStrategy strategy = TransportStrategy.SMTP_PLAIN;

    if (ConnectionSecurity.SSL == cs)
    {
      strategy = TransportStrategy.SMTP_SSL;
    }
    else if (ConnectionSecurity.STARTTLS == cs)
    {
      strategy = TransportStrategy.SMTP_TLS;
    }

    return strategy;
  }

  /**
   * Method description
   *
   *
   * @param old
   */
  private void migrate(NotifyConfiguration old)
  {
    logger.info("migrate notify configuration to mail plugin configuration");

    MailConfiguration config = new MailConfiguration(old.getServer(),
                                 old.getPort(),
                                 convert(old.getConnectionSecurity()),
                                 old.getUsername(), old.getPassword(),
                                 old.getSubjectPrefix());

    mailContext.store(config);
    old.setMigrated(true);
    configurationStore.set(old);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Store<NotifyConfiguration> configurationStore;

  /** Field description */
  private MailContext mailContext;

  /** Field description */
  private MailService mailService;
}
