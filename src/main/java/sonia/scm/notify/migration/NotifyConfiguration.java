/**
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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "notify-configuration")
public class NotifyConfiguration implements Validateable
{

  /**
   * Method description
   *
   *
   * @return
   */
  public ConnectionSecurity getConnectionSecurity()
  {
    return connectionSecurity;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getFrom()
  {
    return from;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getServer()
  {
    return server;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSubjectPrefix()
  {
    return subjectPrefix;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAuthenticationEnabled()
  {
    return Util.isNotEmpty(username) && Util.isNotEmpty(password);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isMigrated()
  {
    return migrated;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(server) && Util.isNotEmpty(from) && (port > 0);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param connectionSecurity
   */
  public void setConnectionSecurity(ConnectionSecurity connectionSecurity)
  {
    this.connectionSecurity = connectionSecurity;
  }

  /**
   * Method description
   *
   *
   * @param from
   */
  public void setFrom(String from)
  {
    this.from = from;
  }

  /**
   * Method description
   *
   *
   * @param migrated
   */
  public void setMigrated(boolean migrated)
  {
    this.migrated = migrated;
  }

  /**
   * Method description
   *
   *
   * @param password
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * Method description
   *
   *
   * @param port
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Method description
   *
   *
   * @param server
   */
  public void setServer(String server)
  {
    this.server = server;
  }

  /**
   * Method description
   *
   *
   * @param subjectPrefix
   */
  public void setSubjectPrefix(String subjectPrefix)
  {
    this.subjectPrefix = subjectPrefix;
  }

  /**
   * Method description
   *
   *
   * @param username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "connection-security")
  private ConnectionSecurity connectionSecurity = ConnectionSecurity.NONE;

  /** Field description */
  private String from;

  /** Field description */
  private String password;

  /** Field description */
  private int port = 25;

  /** Field description */
  private String server;

  /** Field description */
  @XmlElement(name = "subject-prefix")
  private String subjectPrefix = "[SCM]";

  /** Field description */
  private boolean migrated = false;

  /** Field description */
  private String username;
}
