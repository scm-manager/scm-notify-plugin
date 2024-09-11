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

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.search.SearchRequest;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@Path("plugins/notify/search")
public class NotifyMailSearchResource {

  /**
   * Field description
   */
  private static final int MAX_RESULTS = 3;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param userManager
   */
  @Inject
  public NotifyMailSearchResource(UserManager userManager) {
    this.userManager = userManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param queryString
   * @return
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(
    summary = "Get mail search results",
    description = "Returns the mail search results.",
    tags = "Notify Plugin",
    operationId = "notify_get_search_result"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = SearchResults.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public SearchResults search(@QueryParam("query") final String queryString) {
    Subject subject = SecurityUtils.getSubject();

    subject.checkRole(Role.USER);

    if (Strings.isNullOrEmpty(queryString)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    SearchRequest request = new SearchRequest(queryString, true);

    request.setMaxResults(MAX_RESULTS);

    Collection<User> users = userManager.search(request);

    if (users == null) {
      users = ImmutableList.of();
    }

    Collection<SearchResult> results = Collections2.transform(users,
      user -> {
        SearchResult result = null;

        if (!Strings.isNullOrEmpty(user.getMail())) {
          String displayName = user.getDisplayName();

          if (Strings.isNullOrEmpty(displayName)) {
            displayName = user.getMail();
          } else {
            StringBuilder label = new StringBuilder(displayName);

            label.append(" (").append(user.getMail()).append(")");
            displayName = label.toString();
          }

          result = new SearchResult(displayName, user.getMail());
        }

        return result;
      });

    return new SearchResults(results);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 14/02/13
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SearchResult {

    /**
     * Constructs ...
     */
    public SearchResult() {
    }

    /**
     * Constructs ...
     *
     * @param label
     * @param mail
     */
    public SearchResult(String label, String mail) {
      this.label = label;
      this.mail = mail;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     * @return
     */
    public String getLabel() {
      return label;
    }

    /**
     * Method description
     *
     * @return
     */
    public String getMail() {
      return mail;
    }

    //~--- fields -------------------------------------------------------------

    /**
     * Field description
     */
    private String label;

    /**
     * Field description
     */
    private String mail;
  }


  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 14/02/13
   */
  @XmlRootElement(name = "results")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SearchResults {

    /**
     * Constructs ...
     */
    public SearchResults() {
    }

    /**
     * Constructs ...
     *
     * @param results
     */
    public SearchResults(Collection<SearchResult> results) {
      this.results = results;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     * @return
     */
    public Collection<SearchResult> getResults() {
      return results;
    }

    //~--- fields -------------------------------------------------------------

    /**
     * Field description
     */
    private Collection<SearchResult> results;
  }


  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private final UserManager userManager;
}
