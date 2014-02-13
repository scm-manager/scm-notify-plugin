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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.search.SearchRequest;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("plugins/notify/search")
public class NotifyMailSearchResource
{

  /** Field description */
  private static final int MAX_RESULTS = 3;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param userManager
   */
  @Inject
  public NotifyMailSearchResource(UserManager userManager)
  {
    this.userManager = userManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param queryString
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public SearchResults search(@QueryParam("query") final String queryString)
  {
    Subject subject = SecurityUtils.getSubject();

    subject.checkRole(Role.USER);

    if (Strings.isNullOrEmpty(queryString))
    {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    SearchRequest request = new SearchRequest(queryString, true);

    request.setMaxResults(MAX_RESULTS);

    Collection<User> users = userManager.search(request);

    if (users == null)
    {
      users = ImmutableList.of();
    }

    Collection<SearchResult> results = Collections2.transform(users,
                                         new Function<User, SearchResult>()
    {

      @Override
      public SearchResult apply(User user)
      {
        SearchResult result = null;

        if (!Strings.isNullOrEmpty(user.getMail()))
        {
          String displayName = user.getDisplayName();

          if (Strings.isNullOrEmpty(displayName))
          {
            displayName = user.getMail();
          }
          else
          {
            StringBuilder label = new StringBuilder(displayName);

            label.append(" (").append(user.getMail()).append(")");
            displayName = label.toString();
          }

          result = new SearchResult(displayName, user.getMail());
        }

        return result;
      }
    });

    return new SearchResults(results);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/02/13
   * @author         Enter your name here...
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SearchResult
  {

    /**
     * Constructs ...
     *
     */
    public SearchResult() {}

    /**
     * Constructs ...
     *
     *
     * @param label
     * @param mail
     */
    public SearchResult(String label, String mail)
    {
      this.label = label;
      this.mail = mail;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getLabel()
    {
      return label;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getMail()
    {
      return mail;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String label;

    /** Field description */
    private String mail;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/02/13
   * @author         Enter your name here...
   */
  @XmlRootElement(name = "results")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SearchResults
  {

    /**
     * Constructs ...
     *
     */
    public SearchResults() {}

    /**
     * Constructs ...
     *
     *
     * @param results
     */
    public SearchResults(Collection<SearchResult> results)
    {
      this.results = results;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public Collection<SearchResult> getResults()
    {
      return results;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Collection<SearchResult> results;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final UserManager userManager;
}
