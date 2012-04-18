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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHook;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
@Singleton
public class NotifyRepositoryHook extends PostReceiveRepositoryHook
{

  /**
   * the logger for NotifyRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(NotifyRepositoryHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param handlerFactory
   */
  @Inject
  public NotifyRepositoryHook(NotifyContext context,
                              NotifyHandlerFactory handlerFactory)
  {
    this.context = context;
    this.handlerFactory = handlerFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {
    Repository repository = event.getRepository();

    if (repository != null)
    {
      Collection<Changeset> changesets = event.getChangesets();

      if (Util.isNotEmpty(changesets))
      {
        handleEvent(repository, changesets);
      }
      else
      {
        logger.error("received hook without changesets");
      }
    }
    else
    {
      logger.error("received hook without repository");
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changesets
   */
  private void handleEvent(Repository repository,
                           Collection<Changeset> changesets)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("handle notify event for repository {}",
                   repository.getName());
    }

    NotifyRepositoryConfiguration repositoryConfiguration =
      new NotifyRepositoryConfiguration(repository);

    if (repositoryConfiguration.isEnabled())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("send notification for repository {}",
                     repository.getName());
      }

      NotifyConfiguration configuration = context.getConfiguration();
      NotifyHandler handler = handlerFactory.createHandler(configuration,
                                repositoryConfiguration, repository);

      handler.send(changesets);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("notify plugin is disabled for repository {}",
                   repository.getName());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private NotifyContext context;

  /** Field description */
  private NotifyHandlerFactory handlerFactory;
}
