package sonia.scm.notify.api;


import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class NotifyRepositoryConfigurationDto extends HalRepresentation {

  private List<String> contactList = new ArrayList<>();

  private boolean sendToRepositoryContact;

  private boolean useAuthorAsFromAddress;

  private boolean emailPerPush;

  private int maxDiffLines;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
