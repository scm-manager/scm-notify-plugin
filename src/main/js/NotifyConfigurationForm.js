//@flow
import React from "react";
import { translate } from "react-i18next";
import type {PathWPs, PathWP, NotifyConfigurations} from "./NotifyConfigurations";
import { Checkbox } from "@scm-manager/ui-components";
import InputField from "@scm-manager/ui-components/src/forms/InputField";
import LabelWithHelpIcon from "@scm-manager/ui-components/src/forms/LabelWithHelpIcon";
import MemberNameTable from "@scm-manager/ui-components/src/forms/MemberNameTable";
import AddEntryToTableField from "@scm-manager/ui-components/src/forms/AddEntryToTableField";

type Props = {
  initialConfiguration: NotifyConfigurations,
  readOnly: boolean,
  onConfigurationChange: (NotifyConfigurations, boolean) => void,
  // context prop
  t: string => string
};

type State = NotifyConfigurations & {};

class NotifyConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { ...props.initialConfiguration };
  }


  // the configurations are valid if there is at minimum one receiver
  isValid() {
    const { sendToRepositoryContact, contactList } = this.state;
    let valid = true;
    contactList.map(contact => {
      valid = valid && contact.trim() !== "" ;
    });
    return valid || sendToRepositoryContact;
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange({ ...this.state }, this.isValid())
    );
  };

  addContact = (contact) => {
    const contactList = this.state.contactList;
    contactList.push(contact);
    this.configChangeHandler(contactList, "contactList");
  };

  renderCheckboxField = (name: string) => {
    const { t , readOnly} = this.props;
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
    const { t , readOnly} = this.props;
    const fields = [
      "sendToRepositoryContact",
      "useAuthorAsFromAddress",
      "usePrettyDiff",
      "emailPerPush"
    ].map(name => {
      return this.renderCheckboxField(name);
    });

    return (
      <>
        <LabelWithHelpIcon
          label={t("scm-notify-plugin.form.contactList")}
          helpText={t("scm-notify-plugin.formHelpText.contactList")}
        />
        <MemberNameTable
          members={this.state.contactList}
          memberListChanged={(contactList) => {this.setState({contactList})}}
        />
        <AddEntryToTableField
          addEntry={this.addContact}
          disabled={readOnly}
          buttonLabel={t("scm-notify-plugin.form.contactListAdd")}
          errorMessage={t("scm-notify-plugin.form.contactListError")}
        />

        {fields}
        <InputField
          name={"maxDiffLines"}
          label={t("scm-notify-plugin.form.maxDiffLines" )}
          disabled={readOnly}
          value={this.state.maxDiffLines}
          helpText={t("scm-notify-plugin.formHelpText.maxDiffLines")}
          onChange={this.configChangeHandler}
        />
      </>
    );
  }
}

export default translate("plugins")(NotifyConfigurationForm);
