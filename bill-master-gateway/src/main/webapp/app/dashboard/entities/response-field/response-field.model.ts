import { Visibility } from '../../../shared/model/enumerations/visibility.model';
import { RequestType } from '../../../shared/model/enumerations/request-type.model';
import { I18n } from '../../../shared/model/pkbillms/i18n.model';
import { Recipient } from '../recipient/recipient.model';

export enum ResponseFieldType {
  TEXT = 'TEXT',
  AMOUNT = 'AMOUNT',
  DATE = 'DATE',
}

export class ResponseField {
  constructor(
    public id?: number,
    public name?: string,
    public visibility?: Visibility,
    public ord?: number,
    public requestType?: RequestType,
    public labels?: I18n[],
    public recipient?: Recipient,
    public type?: ResponseFieldType,
    public amountRatio?: number
  ) {}
}
