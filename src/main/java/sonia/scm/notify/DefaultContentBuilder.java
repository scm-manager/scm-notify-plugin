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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultContentBuilder extends AbstractContentBuilder {

  private static final String TYPE_SVN = "svn";

  private final RepositoryServiceFactory repositoryServiceFactory;
  private final ScmConfiguration configuration;

  @Inject
  public DefaultContentBuilder(
    ScmConfiguration configuration,
    RepositoryServiceFactory repositoryServiceFactory) {
    this.configuration = configuration;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  @Override
  public Object createModel(Repository repository, NotifyRepositoryConfiguration configuration, Changeset... changesets) throws IOException {
    List<BranchTemplateWrapper> branches;

    try (ChangesetTemplateWrapperHelper helper =
           new ChangesetTemplateWrapperHelper(this.configuration, repositoryServiceFactory, configuration, repository)) {
      branches = helper.wrapAndSortByBranch(changesets);
    }

    Map<String, Object> env = Maps.newHashMap();
    env.put("title", createSubject(repository, changesets));
    env.put("repository", repository);
    env.put("branches", branches);
    env.put("supportNamedBranches", isNamedBranchesSupported(repository));

    return env;
  }

  private boolean isNamedBranchesSupported(Repository repository) {
    return !TYPE_SVN.equalsIgnoreCase(repository.getType());
  }
}
