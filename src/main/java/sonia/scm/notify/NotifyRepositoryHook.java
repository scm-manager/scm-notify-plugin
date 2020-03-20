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

    NotifyRepositoryConfiguration repositoryConfiguration = configurationService.getNotifyConfiguration(repository.getNamespace(), repository.getName());

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
