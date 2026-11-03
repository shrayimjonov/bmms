import { Moment } from 'moment';
import { TransactionStatus } from '../../../dashboard/entities/transaction/transaction.model';

export class SimpleBillResponse {
  constructor(
    public id?: number,
    public recipientShortName?: string,
    public sessionId?: string,
    public amountRatio?: number,
    public amount?: number,
    public account?: string,
    public currency?: number,
    public extId?: string,
    public requestTime?: Moment,
    public responseTime?: Moment,
    public responseText?: string,
    public success?: boolean,
    public status?: TransactionStatus,
    public data?: any,
    public gatewayName?: string,
    public exception?: any
  ) {}
}
