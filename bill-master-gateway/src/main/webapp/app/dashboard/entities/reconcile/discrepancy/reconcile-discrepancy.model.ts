import { Moment } from 'moment';

export class ReconcileDiscrepancy {
  constructor(
    public id?: number,
    public gatewayName?: string,
    public systemTransactionId?: number,
    public gatewayTransactionId?: number,
    public systemAmount?: number,
    public gatewayAmount?: number,
    public systemStatus?: boolean,
    public gatewayStatus?: boolean,
    public systemDate?: Moment,
    public gatewayDate?: Moment,
    public closed?: boolean,
    public comment?: string,
    public reconcileSummaryId?: number
  ) {}
}
