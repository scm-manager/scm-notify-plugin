/*
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
package sonia.scm.notify.service;

import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;

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
