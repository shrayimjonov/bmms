import * as dayjs from 'dayjs';
import {Moment} from "moment";

export enum CurrencyGroup {
  ATM = 'ATM',
  DEFAULT_GROUP = 'DEFAULT_GROUP',
  MOBILE_APP = 'MOBILE_APP',
  EXCHANGE_OFFICE = 'EXCHANGE_OFFICE',
  ABS_FOREX = 'ABS_FOREX',
  TINKOFF = 'TINKOFF',
  TKB = 'TKB',
  CONTACT = 'CONTACT',
  OTHER = 'OTHER',
}

export class Currency {
  constructor(
    public id?: number,
    public code?: string,
    public ccy?: string,
    public nameRu?: string | null,
    public rate?: number | null,
    public sellRate?: number | null,
    public buyRate?: number | null,
    public date?: dayjs.Dayjs | null,
    public active?: boolean,
    public ccyGroup?: CurrencyGroup | string,
    public lastModifiedDate?: Moment,
  ) {}
}

export function getCurrencyIdentifier(currency: Currency): number | undefined {
  return currency.id;
}
