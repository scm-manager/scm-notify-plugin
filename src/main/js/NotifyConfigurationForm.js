//@flow
import React from "react";
import { translate } from "react-i18next";
import type {NotifyConfigurations} from "./NotifyConfigurations";
import { AddEntryToTableField, Checkbox, InputField, LabelWithHelpIcon , MemberNameTagGroup  } from "@scm-manager/ui-components";
import * as validator from "@scm-manager/ui-components/src/validation";

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


  isValid() {
    const { sendToRepositoryContact, contactList } = this.state;
  // the configurations are valid if there is at minimum one receiver
    if (contactList.length === 0 && !sendToRepositoryContact){
      return false
    }
    let valid = true;
    contactList.map(contact => {
      valid = valid && validator.isMailValid(contact) ;
    });
    valid = valid && validator.isNumberValid(this.state.maxDiffLines);
    return valid ;
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
      "emailPerPush"
    ].map(name => {
      return this.renderCheckboxField(name);
    });

    return (
      <>
        <MemberNameTagGroup
          members={this.state.contactList}
          memberListChanged={(contactList) => {
            this.setState({contactList},
              () =>
                this.props.onConfigurationChange({ ...this.state }, this.isValid()));
          }}
          label={t("scm-notify-plugin.form.contactList")}
          helpText={t("scm-notify-plugin.formHelpText.contactList")}
        />
        <AddEntryToTableField
          addEntry={this.addContact}
          disabled={readOnly }
          validateEntry={validator.isMailValid}
          buttonLabel={t("scm-notify-plugin.form.contactListAdd")}
          errorMessage={t("scm-notify-plugin.form.error.contactList")}
        />

        {fields}
        <InputField
          name="maxDiffLines"
          label={t("scm-notify-plugin.form.maxDiffLines" )}
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

export default translate("plugins")(NotifyConfigurationForm);
