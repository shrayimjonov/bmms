import {RecipientSenderType} from '../../../../shared/model/enumerations/recipient-sender-type.model';
import {P2pCondition} from '../condition/p2p-condition.model';
import {P2pCommission} from '../commission/p2p-commission.model';

export class P2pParam {
  constructor(
    public id?: number,
    public priority?: number,
    public name?: string,
    public active?: boolean,
    public senderPc?: RecipientSenderType,
    public recipientPc?: RecipientSenderType,
    public value?: any,
    public senderCondition?: P2pCondition,
    public recipientCondition?: P2pCondition,
    public senderPercent?: P2pCommission,
    public recipientPercent?: P2pCommission,
    public promo?: string
  ) {}
}
