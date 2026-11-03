import {Moment} from 'moment';
import {BktPeriod} from 'app/shared/model/enumerations/bkt-period.model';
import {BookkeepingTransactionType} from 'app/shared/model/enumerations/bookkeeping-transaction-type.model';
import {BookkeepingTransactionStatus} from 'app/shared/model/enumerations/bookkeeping-transaction-status.model';
import {BookkeepingPayType} from 'app/shared/model/enumerations/bookkeeping-pay-type.model';

export class BookkeepingTransaction {
  constructor(
    public id?: number,
    public groupId?: number,
    public sMfo?: string,
    public sInn?: string,
    public sAccount?: string,
    public rMfo?: string,
    public rInn?: string,
    public rAccount?: string,
    public amount?: number,
    public period?: BktPeriod,
    public bookkeepingType?: BookkeepingTransactionType,
    public success?: boolean,
    public details?: string,
    public docType?: string,
    public docCode?: string,
    public docTaskCode?: string,
    public rAccountName?: string,
    public confirmUserID?: string,
    public sendUserID?: string,
    public cancelUserID?: string,
    public createTime?: Moment,
    public confirmTime?: Moment,
    public sendTime?: Moment,
    public cancelTime?: Moment,
    public absId?: number,
    public docId?: number,
    public templateId?: number,
    public control?: number,
    public responseText?: string,
    public type?: BookkeepingTransactionStatus,
    public unionId?: number,
    public groupIds?: string,
    public subRecipient?: string,
    public subRecipientId?: number,
    public comment?: string,
    public bookkeepingPayType?: BookkeepingPayType,
    public params?: string,
    public gatewayName?: string,
    public gatewayId?: number,
    public recipientShortName?: string,
    public recipientId?: number
  ) {}
}
