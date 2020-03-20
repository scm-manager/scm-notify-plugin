/*
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
package sonia.scm.notify.migration;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailService;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class MigrationHandler {

  /** Field description */
  private static final String STORE = "notify";

  /**
   * the logger for MigrationHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MigrationHandler.class);

  private ConfigurationStore<NotifyConfiguration> configurationStore;

  private MailContext mailContext;

  private MailService mailService;

  @Inject
  public MigrationHandler(ConfigurationStoreFactory storeFactory, MailService mailService,
                          MailContext mailContext) {
    this.configurationStore = storeFactory.withType(NotifyConfiguration.class).withName(STORE).build();
    this.mailService = mailService;
    this.mailContext = mailContext;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void migrate() {
    if (!mailService.isConfigured()) {
      NotifyConfiguration configuration = configurationStore.get();

      if (configuration != null) {
        if (!configuration.isMigrated()) {
          migrate(configuration);
        } else if (logger.isDebugEnabled()) {
          logger.debug(
            "notify configuration is already migrated, skip migration");
        }
      } else if (logger.isDebugEnabled()) {
        logger.debug("no notify configuration found, skip migration");
      }
    } else if (logger.isDebugEnabled()) {
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
  private TransportStrategy convert(ConnectionSecurity cs) {
    TransportStrategy strategy = TransportStrategy.SMTP_PLAIN;

    if (ConnectionSecurity.SSL == cs) {
      strategy = TransportStrategy.SMTP_SSL;
    } else if (ConnectionSecurity.STARTTLS == cs) {
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
  private void migrate(NotifyConfiguration old) {
    logger.info("migrate notify configuration to mail plugin configuration");

    MailConfiguration config = new MailConfiguration(old.getServer(),
      old.getPort(),
      convert(old.getConnectionSecurity()),
      old.getFrom(), old.getUsername(),
      old.getPassword(), old.getSubjectPrefix());

    if (config.isValid()) {
      mailContext.store(config);
      old.setMigrated(true);
      configurationStore.set(old);
    } else {
      logger.error(
        "could not migrate configuration, because resulting mail configuration is not valid");
    }
  }

}
