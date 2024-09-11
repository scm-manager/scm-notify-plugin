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

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;


/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class NotifyModule extends AbstractModule
{

  /**
   * the logger for NotifyModule
   */
  private static final Logger logger =
    LoggerFactory.getLogger(NotifyModule.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configure()
  {
    bind(ContentBuilder.class, DefaultContentBuilder.class);
    bind(NotifyHandlerFactory.class, DefaultNotifyHandlerFactory.class);
  }

  /**
   * Method description
   *
   *
   * @param interfaceClass
   * @param implementationClass
   * @param <T>
   */
  private <T> void bind(Class<T> interfaceClass,
                        Class<? extends T> implementationClass)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("bind {} as {}", implementationClass, interfaceClass);
    }

    bind(interfaceClass).to(implementationClass);
  }
}
