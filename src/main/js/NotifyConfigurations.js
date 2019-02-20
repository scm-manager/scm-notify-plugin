// @flow

export type NotifyConfigurations = {
  contactList: string[],
  sendToRepositoryContact :  boolean,
  useAuthorAsFromAddress : boolean,
  usePrettyDiff : boolean,
  emailPerPush : boolean,
  maxDiffLines : boolean
};

