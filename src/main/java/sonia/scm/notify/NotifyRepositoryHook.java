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

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.mail.api.MailService;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfigurationService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class NotifyRepositoryHook {

  /**
   * the logger for NotifyRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(NotifyRepositoryHook.class);

  private NotifyHandlerFactory handlerFactory;

  private MailService mailService;

  private NotifyRepositoryConfigurationService configurationService;


  @Inject
  public NotifyRepositoryHook(MailService mailService,
                              NotifyHandlerFactory handlerFactory, NotifyRepositoryConfigurationService configurationService) {
    this.mailService = mailService;
    this.handlerFactory = handlerFactory;
    this.configurationService = configurationService;
  }

  @Subscribe
  public void onEvent(PostReceiveRepositoryHookEvent event) {
    if (mailService.isConfigured()) {
      handleEvent(event);
    } else if (logger.isWarnEnabled()) {
      logger.warn("smpt server configuration is not valid");
    }
  }

  private void handleEvent(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    HookContext context = event.getContext();

    if (repository != null && context.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)) {
      Iterable<Changeset> changesets = context.getChangesetProvider().getChangesets();

      handleEvent(repository, changesets);
    } else {
      logger.error("received hook without repository");
    }
  }

  private void handleEvent(Repository repository, Iterable<Changeset> changesets) {
    logger.trace("handle notify event for repository {}", repository.getName());

    NotifyRepositoryConfiguration repositoryConfiguration = configurationService.getNotifyConfigurationWithoutPermissionCheck(repository);

    if (repositoryConfiguration.isEnabled()) {
      logger.trace("send notification for repository {}", repository.getName());

      NotifyHandler handler = handlerFactory.createHandler(mailService, repositoryConfiguration, repository);

      if (handler != null) {
        handler.send(changesets);
      } else {
        logger.error("{} returns null instead of a notify handler", handlerFactory.getClass());
      }
    } else {
      logger.debug("notify plugin is disabled for repository {}", repository.getNamespaceAndName());
    }
  }

}
