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



package sonia.scm.notify;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Properties;

import javax.mail.Session;

/**
 *
 * @author Sebastian Sdorra
 */
public class NotifyUtil
{

  /**
   * the logger for NotifyUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(NotifyUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   *
   * @return
   */
  public static Properties createMailProperties(
          NotifyConfiguration configuration)
  {
    Properties properties = new Properties();
    String propertyPrefix = null;

    if (configuration.getConnectionSecurity() == ConnectionSecurity.SSL)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("create mail properties for smtps");
      }

      properties.setProperty("mail.transport.protocol", "smtps");
      propertyPrefix = "mail.smtps.";
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("create mail properties for smtp");
      }

      properties.setProperty("mail.transport.protocol", "smtp");
      propertyPrefix = "mail.smtp.";
    }

    setProperty(properties, propertyPrefix, "host", configuration.getServer());
    setProperty(properties, propertyPrefix, "port", configuration.getPort());

    if (configuration.isAuthenticationEnabled())
    {
      setProperty(properties, propertyPrefix, "auth",
                  configuration.isAuthenticationEnabled());
    }

    if (configuration.getConnectionSecurity() == ConnectionSecurity.STARTTLS)
    {
      setProperty(properties, propertyPrefix, "starttls.enable", true);
    }

    return properties;
  }

  /**
   * Method description
   *
   *
   * @param configuration
   *
   * @return
   */
  public static Session createSession(NotifyConfiguration configuration)
  {
    Session session = null;
    Properties properties = createMailProperties(configuration);

    if (configuration.isAuthenticationEnabled())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create mail session with authentication");
      }

      session = Session.getInstance(properties,
                                    new NotifyAuthenticator(configuration));
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create mail session without authentication");
      }

      session = Session.getInstance(properties);
    }

    return session;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param properties
   * @param prefix
   * @param name
   * @param value
   */
  private static void setProperty(Properties properties, String prefix,
                                  String name, String value)
  {
    String key = prefix.concat(name);

    if (logger.isTraceEnabled())
    {
      logger.trace("create mail property: {} = {}", key, value);
    }

    properties.setProperty(key, value);
  }

  /**
   * Method description
   *
   *
   * @param properties
   * @param prefix
   * @param name
   * @param value
   */
  private static void setProperty(Properties properties, String prefix,
                                  String name, int value)
  {
    setProperty(properties, prefix, name, String.valueOf(value));
  }

  /**
   * Method description
   *
   *
   * @param properties
   * @param prefix
   * @param name
   * @param value
   */
  private static void setProperty(Properties properties, String prefix,
                                  String name, boolean value)
  {
    setProperty(properties, prefix, name, String.valueOf(value));
  }
}
