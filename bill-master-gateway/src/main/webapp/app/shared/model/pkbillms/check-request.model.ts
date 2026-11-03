export class CheckRequest {
  constructor(
    public recipientId: number,
    public account: string,
    public amount: number,
    public currency?: number,
    public agentTranId?: string,
    public requestGroup?: string,
    public requestSubGroup?: string,
    public params?: any,
    public agentParams?: any,
    public amountRatio?: number,
    public gatewayAmount?: number,
    public tranId?: number
  ) {}
}
