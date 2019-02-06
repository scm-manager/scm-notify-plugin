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
