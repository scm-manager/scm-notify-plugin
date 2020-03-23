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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { NotifyConfigurations } from "./NotifyConfigurations";
import {
  validation as validator,
  AddEntryToTableField,
  Checkbox,
  InputField,
  MemberNameTagGroup
} from "@scm-manager/ui-components";

type Props = WithTranslation & {
  initialConfiguration: NotifyConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: NotifyConfigurations, p2: boolean) => void;
};

type State = NotifyConfigurations & {};

class NotifyConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isValid() {
    const { sendToRepositoryContact, contactList } = this.state;
    // the configurations are valid if there is at minimum one receiver
    if (contactList.length === 0 && !sendToRepositoryContact) {
      return false;
    }
    let valid = true;
    contactList.map(contact => {
      valid = valid && validator.isMailValid(contact);
    });
    valid = valid && validator.isNumberValid(this.state.maxDiffLines);
    return valid;
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          this.isValid()
        )
    );
  };

  addContact = contact => {
    const contactList = this.state.contactList;
    contactList.push(contact);
    this.configChangeHandler(contactList, "contactList");
  };

  renderCheckboxField = (name: string) => {
    const { t, readOnly } = this.props;
    return (
      <Checkbox
        label={t("scm-notify-plugin.form." + name)}
        name={name}
        checked={this.state[name]}
        disabled={readOnly}
        helpText={t("scm-notify-plugin.formHelpText." + name)}
        onChange={this.configChangeHandler}
      />
    );
  };

  render() {
    const { t, readOnly } = this.props;
    const fields = ["sendToRepositoryContact", "useAuthorAsFromAddress", "emailPerPush"].map(name => {
      return this.renderCheckboxField(name);
    });

    return (
      <>
        <MemberNameTagGroup
          members={this.state.contactList}
          memberListChanged={contactList => {
            this.setState(
              {
                contactList
              },
              () =>
                this.props.onConfigurationChange(
                  {
                    ...this.state
                  },
                  this.isValid()
                )
            );
          }}
          label={t("scm-notify-plugin.form.contactList")}
          helpText={t("scm-notify-plugin.formHelpText.contactList")}
        />
        <AddEntryToTableField
          addEntry={this.addContact}
          disabled={readOnly}
          validateEntry={validator.isMailValid}
          buttonLabel={t("scm-notify-plugin.form.contactListAdd")}
          errorMessage={t("scm-notify-plugin.form.error.contactList")}
        />

        {fields}
        <InputField
          name="maxDiffLines"
          label={t("scm-notify-plugin.form.maxDiffLines")}
          disabled={readOnly}
          value={this.state.maxDiffLines}
          helpText={t("scm-notify-plugin.formHelpText.maxDiffLines")}
          onChange={this.configChangeHandler}
          validationError={!validator.isNumberValid(this.state.maxDiffLines)}
          errorMessage={t("scm-notify-plugin.form.error.maxDiffLines")}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(NotifyConfigurationForm);
