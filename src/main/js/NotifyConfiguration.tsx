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
import { Configuration } from "@scm-manager/ui-components";
import { Subtitle } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import NotifyConfigurationForm from "./NotifyConfigurationForm";

type Props = {
  link: string;
};

const NotifyConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");

  return (
    <>
      <Subtitle subtitle={t("scm-notify-plugin.form.header")} />
      <br />
      <Configuration link={link} render={props => <NotifyConfigurationForm {...props} />} />
    </>
  );
};

export default NotifyConfiguration;
