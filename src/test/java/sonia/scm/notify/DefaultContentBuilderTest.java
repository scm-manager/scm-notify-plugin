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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryServiceFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultContentBuilderTest {

  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private RepositoryServiceFactory serviceFactory;

  @InjectMocks
  private DefaultContentBuilder builder;

  @Test
  void shouldCreateSubject() {
    Repository repository = RepositoryTestData.create42Puzzle();

    String subject = builder.createSubject(
      repository,
      new Changeset("0", 0L, Person.toPerson("trillian")),
      new Changeset("1", 0L, Person.toPerson("trillian"))
    );

    assertThat(subject).isEqualTo("[42Puzzle][default] 0,1");
  }

  @Test
  void shouldCreateSubjectOverMaxCharLimit() {
    Repository repository = RepositoryTestData.createHappyVerticalPeopleTransporter();

    Changeset ch1 = new Changeset("0", 0L, Person.toPerson("trillian"));
    ch1.setBranches(ImmutableList.of("feature/top_secret_most_important_database_integration", "feature/also_important_but_not_as_much"));

    String subject = builder.createSubject(
      repository,
      ch1
    );

    assertThat(subject).isEqualTo("[happyVerticalPeopleTransporter][feature/top_secret_most_important_database_integration...");
  }

  @Test
  void shouldCreateSubjectWithSingleBranchHittingTheLimit() {
    Repository repository = RepositoryTestData.createHeartOfGold();

    Changeset ch1 = new Changeset("0", 0L, Person.toPerson("trillian"));
    ch1.setBranches(ImmutableList.of("feature/with_really_long_i_mean_gigantic_long_feature_eh_i_mean_branch_name_dot_yes_it_is_very_long_v2"));

    String subject = builder.createSubject(
      repository,
      ch1
    );

    assertThat(subject).isEqualTo("[HeartOfGold][feature/with_really_long_i_mean_gigantic_long_feature_eh_i_mean_branch_name_dot_yes...");
  }

}
