import { Moment } from 'moment';
import { RequestType } from '../../../shared/model/enumerations/request-type.model';

export class Bill {
  constructor(
    public id?: number,
    public recipientId?: number,
    public recipientShortName?: string,
    public sessionId?: string,
    public type?: RequestType,
    public amountRatio?: number,
    public amount?: number,
    public profit?: number,
    public account?: string,
    public params?: any,
    public currency?: number,
    public extId?: string,
    public checkId?: string,
    public payId?: string,
    public requestTime?: Moment,
    public requestJson?: string,
    public responseJson?: string,
    public responseTime?: Moment,
    public responseCode?: string,
    public responseText?: string,
    public success?: boolean,
    public login?: string,
    public fraudId?: number,
    public transactionId?: number
  ) {}
}
