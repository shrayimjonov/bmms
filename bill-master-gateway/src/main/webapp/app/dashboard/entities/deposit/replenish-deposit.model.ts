export class ReplenishDeposit {
  constructor(
    public agentLogin?: string,
    public overdraftLimit?: number,
    public balanceAmount?: number,
    public doc?: string,
    public payerTaxnr?: string,
    public recipientTaxnr?: string
  ) {}
}
