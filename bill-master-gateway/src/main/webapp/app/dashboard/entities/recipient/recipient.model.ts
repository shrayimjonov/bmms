import { Gateway } from '../gateway/gateway.model';
import { Category } from '../category/category.model';
import { Field } from '../field/field.model';
import { I18n } from '../../../shared/model/pkbillms/i18n.model';

export enum RecipientType {
  SIMPLE = 'SIMPLE',
  GIFT = 'GIFT',
  PIN = 'PIN',
  AMOUNT_DICTATOR = 'AMOUNT_DICTATOR',
  INVOICE = 'INVOICE',
  OTHER = 'OTHER',
}

export class Recipient {
  constructor(
    public id?: number,
    public shortName?: string,
    public serviceId?: string,
    public thumbnail?: string,
    public logo?: string,
    public amountRatio?: number,
    public ccy?: number,
    public minAmount?: number,
    public maxAmount?: number,
    public limitRatio?: number,
    public commission?: number,
    public fee?: number,
    public systemCommission?: number,
    public systemFee?: number,
    public senderCommission?: number,
    public ord?: number,
    public active?: boolean,
    public check?: boolean,
    public pay?: boolean,
    public status?: boolean,
    public cancel?: boolean,
    public canDeposit?: boolean,
    public eposPort?: number,
    public eposMerchant?: number,
    public eposTerminal?: number,
    public settlementAccount?: string,
    public mfo?: string,
    public inn?: string,
    public reconcile?: boolean,
    public gateway?: Gateway,
    public category?: Category,
    public titles?: I18n[],
    public fullNames?: I18n[],
    public createClient?: boolean,
    public phoneNumber?: string,
    public type?: RecipientType,
    public fields?: Field[],
    public jsonParams?: any
  ) {}
}
