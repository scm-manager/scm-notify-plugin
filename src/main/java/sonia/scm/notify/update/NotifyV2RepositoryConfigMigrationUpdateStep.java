package sonia.scm.notify.update;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.notify.service.NotifyRepositoryConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class NotifyV2RepositoryConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(NotifyV2RepositoryConfigMigrationUpdateStep.class);


  V1PropertyDAO v1PropertyDAO;
  ConfigurationStoreFactory storeFactory;

  private static final String STORE_NAME = "NotifyConfigurations";

  private static final String NOTIFY_REPOSITORY_CONTACT = "notify.contact.repository";
  private static final String NOTIFY_MAX_DIFF_LINES = "notify.max.diff.lines";
  private static final String NOTIFY_CONTACT_LIST = "notify.contact.list";
  private static final String NOTIFY_EMAIL_PER_PUSH = "notify.email.per.push";
  private static final String NOTIFY_USE_AUTHOR = "notify.use.author.as.from.address";

  @Inject
  public NotifyV2RepositoryConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, ConfigurationStoreFactory storeFactory) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() throws IOException {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAnyOf(NOTIFY_REPOSITORY_CONTACT, NOTIFY_MAX_DIFF_LINES, NOTIFY_CONTACT_LIST, NOTIFY_EMAIL_PER_PUSH, NOTIFY_USE_AUTHOR)
      .forEachEntry((key, properties) -> createConfigStore(key).set(buildConfig(key, properties)));
  }

  private NotifyRepositoryConfiguration buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific notify configuration for repository id {}", repositoryId);

    List<String> contactList = new ArrayList<>();
    String v1ContactList = properties.get(NOTIFY_CONTACT_LIST);
    if (v1ContactList != null && !v1ContactList.isEmpty()) {
      String[] splittedContactList = v1ContactList.split(";");
      for (String contact : splittedContactList) {
        contactList.add(contact);
      }
    }

    NotifyRepositoryConfiguration v2NotifyRepositoryConfiguration = new NotifyRepositoryConfiguration();
    v2NotifyRepositoryConfiguration.setContactList(contactList);
    v2NotifyRepositoryConfiguration.setEmailPerPush(Boolean.parseBoolean(properties.get(NOTIFY_EMAIL_PER_PUSH)));
    v2NotifyRepositoryConfiguration.setMaxDiffLines(Integer.parseInt(properties.get(NOTIFY_MAX_DIFF_LINES)));
    v2NotifyRepositoryConfiguration.setSendToRepositoryContact(Boolean.parseBoolean(properties.get(NOTIFY_REPOSITORY_CONTACT)));
    v2NotifyRepositoryConfiguration.setUseAuthorAsFromAddress(Boolean.parseBoolean(properties.get(NOTIFY_USE_AUTHOR)));

    return v2NotifyRepositoryConfiguration;
  }

  private ConfigurationStore<NotifyRepositoryConfiguration> createConfigStore(String repositoryId) {
    return storeFactory.withType(NotifyRepositoryConfiguration.class).withName(STORE_NAME).forRepository(repositoryId).build();
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.notify.repository.config.xml";
  }
}
