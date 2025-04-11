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
import { Configuration } from "@scm-manager/ui-components";
import { Subtitle, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import NotifyConfigurationForm from "./NotifyConfigurationForm";

type Props = {
  repository: Repository;
  link: string;
};

export default function NotifyConfigurationContainerFC({ repository, link }: Readonly<Props>) {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-notify-plugin.form.header"));
  return (
    <>
      <Subtitle subtitle={t("scm-notify-plugin.form.header")} />
      <br />
      <Configuration link={link} render={(props) => <NotifyConfigurationForm {...props} t={t} />} />
    </>
  );
}
