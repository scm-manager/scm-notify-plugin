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

package sonia.scm.notify.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.util.Util;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "notify-configurations")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class NotifyRepositoryConfiguration {

  private List<String> contactList = new ArrayList<>();
  private boolean sendToRepositoryContact;
  private boolean useAuthorAsFromAddress;
  private boolean emailPerPush;
  private int maxDiffLines;

  public boolean isEnabled() {
    return Util.isNotEmpty(contactList) || sendToRepositoryContact;
  }
}
