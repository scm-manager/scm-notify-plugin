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

import com.google.inject.Inject;
import com.google.inject.Provider;
import sonia.scm.plugin.Extension;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class MigrationContextListener implements ServletContextListener
{

  /**
   * Constructs ...
   *
   *
   *
   * @param migrationHandlerProvider
   */
  @Inject
  public MigrationContextListener(
    Provider<MigrationHandler> migrationHandlerProvider)
  {
    this.migrationHandlerProvider = migrationHandlerProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    migrationHandlerProvider.get().migrate();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<MigrationHandler> migrationHandlerProvider;
}
