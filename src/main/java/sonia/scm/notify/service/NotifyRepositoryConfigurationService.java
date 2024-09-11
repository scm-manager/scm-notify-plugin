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

package sonia.scm.notify.service;

import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;

/**
 * Store the notify repository configurations in the repository store.
 *
 * @author Mohamed Karray
 */
public class NotifyRepositoryConfigurationService {

  public static final String PERMISSION = "repository:notify";
  private ConfigurationStoreFactory storeFactory;
  private RepositoryServiceFactory repositoryServiceFactory;
  private static final String STORE_NAME = "NotifyConfigurations";

  @Inject
  public NotifyRepositoryConfigurationService(ConfigurationStoreFactory storeFactory, RepositoryServiceFactory repositoryServiceFactory) {
    this.storeFactory = storeFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  public static boolean isPermitted(Repository repository) {
    return SecurityUtils.getSubject().isPermitted(PERMISSION + ":" + repository.getId());
  }

  public void checkPermission(Repository repository) {
    SecurityUtils.getSubject().checkPermission(PERMISSION + ":" + repository.getId());
  }

  private ConfigurationStore<NotifyRepositoryConfiguration> getStore(Repository repository) {
    return storeFactory.withType(NotifyRepositoryConfiguration.class).withName(STORE_NAME).forRepository(repository).build();
  }

  private Repository getRepository(String namespace, String name) {
    Repository repository;
    try (RepositoryService repositoryService = repositoryServiceFactory.create(new NamespaceAndName(namespace, name))) {
      repository = repositoryService.getRepository();
    }
    return repository;
  }

  public NotifyRepositoryConfiguration getNotifyConfiguration(String namespace, String name) {
    Repository repository = getRepository(namespace, name);
    checkPermission(repository);
    return getNotifyConfigurationWithoutPermissionCheck(repository);
  }

  public NotifyRepositoryConfiguration getNotifyConfigurationWithoutPermissionCheck(Repository repository) {
    ConfigurationStore<NotifyRepositoryConfiguration> store = getStore(repository);
    NotifyRepositoryConfiguration configuration = store.get();
    if (configuration == null) {
      configuration = new NotifyRepositoryConfiguration();
      store.set(configuration);
    }
    return configuration;
  }

  public void setConfiguration(String namespace, String name, NotifyRepositoryConfiguration configuration) {
    setConfiguration(getRepository(namespace, name), configuration);

  }

  public void setConfiguration(Repository repository, NotifyRepositoryConfiguration configuration) {
    checkPermission(repository);
    ConfigurationStore<NotifyRepositoryConfiguration> store = getStore(repository);
    store.set(configuration);
  }
}
