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

import sonia.scm.PropertiesAware;
import sonia.scm.util.Util;

/**
 *
 * @author Sebastian Sdorra
 */
public class NotifyRepositoryConfiguration
{

  /** Field description */
  public static final String PROPERTY_CONTACT_PRIVILEGED =
    "notify.contact.privileged";

  /** Field description */
  public static final String PROPERTY_CONTACT_REPOSITORY =
    "notify.contact.repository";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param properties
   */
  public NotifyRepositoryConfiguration(PropertiesAware properties)
  {
    sendToPrivilegedUsers = getBooleanProperty(properties,
            PROPERTY_CONTACT_PRIVILEGED);
    sendToRepositoryContact = getBooleanProperty(properties,
            PROPERTY_CONTACT_REPOSITORY);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnabled()
  {
    return sendToPrivilegedUsers || sendToRepositoryContact;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSendToPrivilegedUsers()
  {
    return sendToPrivilegedUsers;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSendToRepositoryContact()
  {
    return sendToRepositoryContact;
  }

  /**
   * Method description
   *
   *
   * @param properties
   * @param name
   *
   * @return
   */
  private boolean getBooleanProperty(PropertiesAware properties, String name)
  {
    boolean result = false;
    String value = properties.getProperty(name);

    if (Util.isNotEmpty(value))
    {
      result = Boolean.parseBoolean(value);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean sendToPrivilegedUsers;

  /** Field description */
  private boolean sendToRepositoryContact;
}
