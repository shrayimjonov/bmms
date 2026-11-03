import { Recipient } from '../recipient/recipient.model';
import { Agent } from '../agent/agent.model';
import { RecipientGroup } from '../recipient-group/recipient-group.model';
import { AgentRecipientSetting } from '../agent-recipient-setting/agent-recipient-setting.model';

export class AgentRecipient {
  constructor(
    public id?: number,
    public agentCommission?: number,
    public agentFee?: number,
    public systemCommissionUp?: number,
    public systemCommissionDown?: number,
    public systemFee?: number,
    public active?: boolean,
    public eposPort?: number,
    public eposMerchant?: string,
    public eposTerminal?: string,
    public requestGroup?: string,
    public jsonParams?: any,
    public setting?: AgentRecipientSetting,
    public recipientGroup?: RecipientGroup,
    public agent?: Agent,
    public recipient?: Recipient
  ) {}
}
