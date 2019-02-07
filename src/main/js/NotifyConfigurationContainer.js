// @flow
import React from "react";
import { Configuration, Subtitle } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";
import NotifyConfigurationForm from "./NotifyConfigurationForm";

type Props = {
  repository: Repository,
  link: string,
  t: string => string
};

class NotifyConfigurationContainer extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    const { t, link} = this.props;
    return (
      <>
        <Subtitle subtitle={t("scm-notify-plugin.form.header")} />
        <br />
        <Configuration
          link={link}
          render={props => (
            <NotifyConfigurationForm
              {...props}
            />
          )}
        />
      </>
    );
  }
}

export default translate("plugins")(NotifyConfigurationContainer);
