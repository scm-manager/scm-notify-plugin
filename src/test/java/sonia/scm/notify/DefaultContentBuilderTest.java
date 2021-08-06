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
}
