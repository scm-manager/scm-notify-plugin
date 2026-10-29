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

import React, { ComponentProps, FC, KeyboardEvent, ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller } from "react-hook-form";
import {
  useScmFormContext,
  ConfigurationSubpageForm,
  Form,
  RadioGroup,
  Field,
  Label,
  Button,
  Icon,
} from "@scm-manager/ui-core";
import { validation as validator } from "@scm-manager/ui-components";
import { NotifyConfigurations } from "./NotifyConfigurations";
import styled, { createGlobalStyle } from "styled-components";

const DiffInput = styled(Form.Input)`input {width: 100px}`

const Divider = styled.hr`
  height: 1px;
  background: var(--scm-border-color);
`

const OverviewStyle = createGlobalStyle`
  @media screen and (max-width: 1023px) {
    .level {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    .responsive-overview-table tr {
      display: block !important;
      padding: 0.65rem 0;
    }
    .responsive-overview-table th,
    .responsive-overview-table td {
      display: block !important;
      width: 100% !important;
    }
    .settings-table.responsive-overview-table th {
      border: none !important;
      padding: 0 1rem;
      color: var(--scm-text-color);
    }
    .contacts-list-table tr:not(:last-child) {
      padding: 0;
    }
  }
`

const SubpageStyle = createGlobalStyle`
  .field {
    display:inline-block;
    width:100%;
    margin-bottom:0 !important;
  }

  .help.is-danger {
    position: relative;
  }

  .has-addons{
    width: calc(100% + .75rem);
  }

  @media screen and (max-width: 768px) {
    .responsive-tr {
      display: block !important;
      padding: 0.75rem 0;
      border-bottom: 1px solid var(--scm-border-color) !important;
    }
    .responsive-td {
      display: block !important;
      border: none !important;
      padding: 0.25rem 0 !important;
    }

    .control {
      margin: 0 !important;
      border-radius: 4px !important;
    }

    .control:last-child {
      padding-top: 0;
    }

    .control input, .control button:not([role="radio"]){
      margin: 0 !important;
      border-radius: 4px !important;
    }
  }

  @media screen and (min-width: 769px) {

    .control{
      padding-right: 0;
    }
    .control:last-child{
      padding-left: 0;
    }
  }
`
const Overview: FC<{ children?: ReactNode }> = ({ children }) => (
  <ConfigurationSubpageForm.Overview>
    <OverviewStyle />
    {children}
  </ConfigurationSubpageForm.Overview>
);

const Subpage: FC<ComponentProps<typeof ConfigurationSubpageForm.Subpage>> = ({ children, ...props }) => (
  <ConfigurationSubpageForm.Subpage {...props}>
    <SubpageStyle />
    {children}
  </ConfigurationSubpageForm.Subpage>
);

const BooleanRadioField: FC<{
  name: keyof NotifyConfigurations & string;
  label: string;
  description?: string;
  falseLabel: string;
  trueLabel: string;
}> = ({ name, label, description, falseLabel, trueLabel }) => {
  const { control, readOnly } = useScmFormContext();
  return (
    <Form.Row className="mt-0 mb-0">
      <Field className="column pt-0 pb-0" as="fieldset">
        <Label as="legend">{label}</Label>
        {description ? <p className="mb-2 is-size-7">{description}</p> : null}
        <Controller
          control={control}
          name={name}
          render={({ field }) => (
            <RadioGroup
              name={name}
              disabled={readOnly}
              value={field.value ? "true" : "false"}
              onValueChange={(value: string) => field.onChange(value === "true")}
            >
              <RadioGroup.Option value="false" label={falseLabel} />
              <RadioGroup.Option value="true" label={trueLabel} />
            </RadioGroup>
          )}
        />
      </Field>
    </Form.Row>
  );
};

const NotifyOverview: FC = () => {
  const [t] = useTranslation("plugins");
  const { watch } = useScmFormContext();
  const config = watch() as NotifyConfigurations;
  const contactList = config.contactList ?? [];
  return (
    <Overview>
      <ConfigurationSubpageForm.Subpage.Header
        subpage="contacts"
        label={t("scm-notify-plugin.subpage.overview.contactsLabel")}
      >
        {t("scm-notify-plugin.subpage.overview.editContacts")}
      </ConfigurationSubpageForm.Subpage.Header>
      <table className="table is-fullwidth mb-5 responsive-overview-table contacts-list-table" role="table">
        <thead role="rowgroup">
        <tr role="row">
          <th role="columnheader">{t("scm-notify-plugin.subpage.overview.emailHeader")}</th>
        </tr>
        </thead>
        <tbody role="rowgroup">
        {contactList.length === 0 ? (
          <tr role="row">
            <td className="has-text-grey" role="cell">
              {t("scm-notify-plugin.subpage.overview.noContacts")}
            </td>
          </tr>
        ) : (
          contactList.map((email) => (
            <tr key={email} role="row">
              <td role="cell" style={{ wordBreak: "break-all" }}>{email}</td>
            </tr>
          ))
        )}
        </tbody>
      </table>
      <Divider />
      <ConfigurationSubpageForm.Subpage.Header
        subpage="settings"
        label={t("scm-notify-plugin.subpage.overview.settingsLabel")}
      >
        {t("scm-notify-plugin.subpage.overview.editSettings")}
      </ConfigurationSubpageForm.Subpage.Header>
      <table className="table is-fullwidth responsive-overview-table settings-table" role="table">
        <tbody role="rowgroup">
        <tr role="row">
          <th role="rowheader">{t("scm-notify-plugin.subpage.overview.sendAs")}</th>
          <td role="cell">
            {config.useAuthorAsFromAddress
              ? t("scm-notify-plugin.subpage.settings.sendAs.author")
              : t("scm-notify-plugin.subpage.settings.sendAs.system")}
          </td>
        </tr>
        <tr role="row">
          <th role="rowheader">{t("scm-notify-plugin.subpage.overview.notifyOn")}</th>
          <td role="cell">
            {config.emailPerPush
              ? t("scm-notify-plugin.subpage.settings.notifyOn.perPush")
              : t("scm-notify-plugin.subpage.settings.notifyOn.perCommit")}
          </td>
        </tr>
        <tr role="row">
          <th role="rowheader">{t("scm-notify-plugin.subpage.overview.repositoryContact")}</th>
          <td role="cell">
            {config.sendToRepositoryContact
              ? t("scm-notify-plugin.subpage.overview.yes")
              : t("scm-notify-plugin.subpage.overview.no")}
          </td>
        </tr>
        <tr role="row">
          <th role="rowheader">{t("scm-notify-plugin.subpage.overview.maxDiffLines")}</th>
          <td role="cell">{config.maxDiffLines}</td>
        </tr>
        </tbody>
      </table>
    </Overview>
  );
};

const ContactListSubpage: FC = () => {
  const [t] = useTranslation("plugins");
  const { watch, setValue, readOnly } = useScmFormContext();
  const contactList: string[] = watch("contactList") ?? [];
  const [newEmail, setNewEmail] = useState("");

  const isValidEmail = validator.isMailValid(newEmail) && /^[\x00-\x7F]+$/.test(newEmail);
  const isDuplicate = contactList.some((entry) => entry.toLowerCase() === newEmail.toLowerCase());
  const canAdd = !readOnly && isValidEmail && !isDuplicate;

  const updateContactList = (next: string[]) =>
    setValue("contactList", next, { shouldDirty: true, shouldValidate: true });

  const addContact = () => {
    if (canAdd) {
      updateContactList([...contactList, newEmail]);
      setNewEmail("");
    }
  };

  const removeContact = (email: string) => updateContactList(contactList.filter((entry) => entry !== email));

  const onInputKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      addContact();
    }
  };

  return (
    <Subpage name="contacts" title={t("scm-notify-plugin.subpage.contacts.title")}>
      <h4 className="subtitle is-5">{t("scm-notify-plugin.subpage.contacts.addTitle")}</h4>
      <Field>
        <Label>{t("scm-notify-plugin.subpage.contacts.addressLabel")}</Label>
        <div className="field has-addons columns is-mobile is-multiline is-vcentered">
          <div className="control is-expanded mr-2 column is-12-mobile">
            <input
              className="input"
              type="email"
              value={newEmail}
              disabled={readOnly}
              onChange={(event) => setNewEmail(event.target.value)}
              onKeyDown={onInputKeyDown}
            />
          </div>
          <div className="control column is-12-mobile is-narrow-tablet">
            <Button className="is-fullwidth" onClick={addContact} disabled={!canAdd}>
              <Icon className="mr-1">plus</Icon>
              {t("scm-notify-plugin.subpage.contacts.addButton")}
            </Button>
          </div>
        </div>
        {newEmail.length > 0 && !isValidEmail ? (
          <p className="help is-danger">{t("scm-notify-plugin.subpage.contacts.invalidEmail")}</p>
        ) : null}
        {isValidEmail && isDuplicate ? (
          <p className="help is-danger">{t("scm-notify-plugin.subpage.contacts.duplicateEmail")}</p>
        ) : null}
      </Field>
      <Divider />
      <h4 className="subtitle is-5">{t("scm-notify-plugin.subpage.contacts.removeTitle")}</h4>
      <table className="table is-hoverable is-fullwidth" role="table">
        <thead className="is-hidden-mobile" role="rowgroup">
        <tr role="row">
          <th role="columnheader">{t("scm-notify-plugin.subpage.contacts.emailHeader")}</th>
          <th role="columnheader" />
        </tr>
        </thead>
        <tbody role="rowgroup">
        {contactList.length === 0 ? (
          <tr role="row">
            <td className="has-text-grey responsive-td" role="cell">{t("scm-notify-plugin.subpage.overview.noContacts")}</td>
          </tr>
        ) : (
          contactList.map((email) => (
            <tr key={email} className="responsive-tr" role="row">
              <td className="is-vcentered responsive-td" role="cell" style={{ wordBreak: "break-all" }}>
                {email}
              </td>
              <td className="has-text-right responsive-td" role="cell">
                <Button variant="ghost" onClick={() => removeContact(email)} disabled={readOnly}>
                  <Icon className="mr-1">trash</Icon>
                  {t("scm-notify-plugin.subpage.contacts.delete")}
                </Button>
              </td>
            </tr>
          )))}
        </tbody>
      </table>
      <Divider />
    </Subpage>
  );
};

const SettingsSubpage: FC = () => {
  const [t] = useTranslation("plugins");

  const maxDiffLinesError = t("scm-notify-plugin.subpage.settings.maxDiffLines.error");

  return (
    <Subpage name="settings" title={t("scm-notify-plugin.subpage.settings.title")}>
      <BooleanRadioField
        name="useAuthorAsFromAddress"
        label={t("scm-notify-plugin.subpage.settings.sendAs.label")}
        description={t("scm-notify-plugin.subpage.settings.sendAs.description")}
        falseLabel={t("scm-notify-plugin.subpage.settings.sendAs.system")}
        trueLabel={t("scm-notify-plugin.subpage.settings.sendAs.author")}
      />
      <Divider />
      <BooleanRadioField
        name="emailPerPush"
        label={t("scm-notify-plugin.subpage.settings.notifyOn.label")}
        falseLabel={t("scm-notify-plugin.subpage.settings.notifyOn.perCommit")}
        trueLabel={t("scm-notify-plugin.subpage.settings.notifyOn.perPush")}
      />
      <Divider />
      <BooleanRadioField
        name="sendToRepositoryContact"
        label={t("scm-notify-plugin.subpage.settings.repositoryContact.label")}
        falseLabel={t("scm-notify-plugin.subpage.settings.repositoryContact.no")}
        trueLabel={t("scm-notify-plugin.subpage.settings.repositoryContact.yes")}
      />
      <Divider />
      <Form.Row>
        <DiffInput
          name="maxDiffLines"
          type="number"
          min={0}
          max={999}
          label={t("scm-notify-plugin.subpage.settings.maxDiffLines.label")}
          descriptionText={t("scm-notify-plugin.subpage.settings.maxDiffLines.description")}
          rules={{
            validate: (value: unknown) => {
              const numberValue = Number(value);
              const valid =
                value !== "" &&
                value !== null &&
                Number.isInteger(numberValue) &&
                numberValue >= 0 &&
                numberValue <= 999;
              return valid || maxDiffLinesError;
            },
          }}
        />
      </Form.Row>
      <Divider />
    </Subpage>
  );
};

const NotifyConfigurationForm: FC = () => (
  <>
    <NotifyOverview />
    <ContactListSubpage />
    <SettingsSubpage />
  </>
);

export default NotifyConfigurationForm;
