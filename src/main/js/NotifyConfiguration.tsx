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

import React, { FC } from "react";
import { ConfigurationSubpageForm, useRepositoryFromParams } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { Link } from "@scm-manager/ui-types";
import NotifyConfigurationForm from "./NotifyConfigurationForm";
import { NotifyConfigurations } from "./NotifyConfigurations";

const NotifyConfiguration: FC = () => {
  const [t] = useTranslation("plugins");
  const repository = useRepositoryFromParams();
  const link = (repository._links["notifyConfig"] as Link).href;

  return (
    <ConfigurationSubpageForm<NotifyConfigurations>
      link={link}
      title={t("scm-notify-plugin.subpage.title")}
      translationPath={["plugins", "scm-notify-plugin.subpage"]}
    >
      <NotifyConfigurationForm />
    </ConfigurationSubpageForm>
  );
};

export default NotifyConfiguration;
