import { PayType } from '../enumerations/pay-type.model';

export class PayRequest {
  constructor(
    public billId: number,
    public amount?: number,
    public type?: PayType,
    public gatewayAmount?: number,
    public settingId?: number,
    public params?: any,
    public agentParams?: any,
    public tranId?: number
  ) {}
}
