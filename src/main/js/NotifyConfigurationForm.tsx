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

import React from "react";
import { TFunction } from "react-i18next";
import { NotifyConfigurations } from "./NotifyConfigurations";
import {
  validation as validator,
  AddEntryToTableField,
  Checkbox,
  InputField,
  MemberNameTagGroup,
} from "@scm-manager/ui-components";

type Props = {
  initialConfiguration: NotifyConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: NotifyConfigurations, p2: boolean) => void;
  t: TFunction<"plugins", undefined>;
};

type State = NotifyConfigurations;

class NotifyConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
    };
  }

  isValid() {
    const { contactList } = this.state;
    let valid = true;
    contactList.forEach((contact) => {
      valid = valid && validator.isMailValid(contact);
    });
    valid = valid && validator.isNumberValid(this.state.maxDiffLines);
    return valid;
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value,
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state,
          },
          this.isValid(),
        ),
    );
  };

  addContact = (contact) => {
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
    const fields = ["sendToRepositoryContact", "useAuthorAsFromAddress", "emailPerPush"].map((name) => {
      return this.renderCheckboxField(name);
    });

    return (
      <>
        <MemberNameTagGroup
          members={this.state.contactList}
          memberListChanged={(contactList) => {
            this.setState(
              {
                contactList,
              },
              () =>
                this.props.onConfigurationChange(
                  {
                    ...this.state,
                  },
                  this.isValid(),
                ),
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

export default NotifyConfigurationForm;
