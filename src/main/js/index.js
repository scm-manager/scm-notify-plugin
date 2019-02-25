// @flow

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import NotifyConfigurationContainer from "./NotifyConfigurationContainer";

cfgBinder.bindRepositorySetting(
  "/notify",
  "scm-notify-plugin.navLink",
  "notifyConfig",
  NotifyConfigurationContainer
);