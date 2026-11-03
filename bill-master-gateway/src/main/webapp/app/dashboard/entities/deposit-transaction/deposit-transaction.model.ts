import { Moment } from 'moment';

export class DepositTransaction {
  constructor(
    public id?: number,
    public payerTaxnr?: string,
    public recipientTaxnr?: string,
    public amount?: number,
    public updateDate?: Moment,
    public comment?: string,
    public doc?: string,
    public agentLogin?: string
  ) {}
}
