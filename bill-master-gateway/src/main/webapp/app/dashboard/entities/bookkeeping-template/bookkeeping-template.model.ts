import { BktPeriod } from '../../../shared/model/enumerations/bkt-period.model';
import { PayType } from '../../../shared/model/enumerations/pay-type.model';
import { BookkeepingPayType } from '../../../shared/model/enumerations/bookkeeping-pay-type.model';
import { BookkeepingTransactionType } from '../../../shared/model/enumerations/bookkeeping-transaction-type.model';
import { Gateway } from '../gateway/gateway.model';
import { Recipient } from '../recipient/recipient.model';

export enum BookkeepingAmountCalculateType {
  RECIPIENT = 'RECIPIENT',
  GATEWAY = 'GATEWAY',
  SUB_RECIPIENT = 'SUB_RECIPIENT',
  PAY_TYPE = 'PAY_TYPE',
}

export class BookkeepingTemplate {
  constructor(
    public groupId?: number,
    public sMfo?: string,
    public sInn?: string,
    public sAccount?: string,
    public rMfo?: string,
    public rInn?: string,
    public rAccount?: string,
    public calculateAmountRule?: string,
    public period?: BktPeriod,
    public payTypes?: PayType[],
    public desc?: string,
    public sType?: number,
    public sTypeIds?: string,
    public bookkeepingType?: BookkeepingTransactionType,
    public name?: string,
    public amountCalculateType?: BookkeepingAmountCalculateType,
    public docType?: string,
    public docCode?: string,
    public docTaskCode?: string,
    public rAccountName?: string,
    public bookkeepingPayType?: BookkeepingPayType,
    public groupIds?: string,
    public control?: number,
    public subRecipientId?: number,
    public id?: number,
    public gateway?: Gateway,
    public recipient?: Recipient
  ) {}
}
