import React from "react";
import { Configuration, Subtitle } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import NotifyConfigurationForm from "./NotifyConfigurationForm";

type Props = WithTranslation & {
  repository: Repository;
  link: string;
};

class NotifyConfigurationContainer extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Subtitle subtitle={t("scm-notify-plugin.form.header")} />
        <br />
        <Configuration link={link} render={props => <NotifyConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(NotifyConfigurationContainer);
