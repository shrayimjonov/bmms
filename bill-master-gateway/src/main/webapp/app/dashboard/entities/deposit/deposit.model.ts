import { Agent } from '../agent/agent.model';
import { Gateway } from '../gateway/gateway.model';
import { StateEnum } from '../user-management/user/user.model';

export class Deposit {
  constructor(
    public id?: number,
    public overdraftLimit?: number,
    public balance?: number,
    public balanceCcy?: number,
    public bookedBalance?: number,
    public limitInform?: number,
    public limitDisable?: number,
    public contactGroup?: string,
    public blocked?: boolean,
    public unlimit?: boolean,
    public gatewayName?: string,
    public gatewayId?: number,
    public agentShortName?: string,
    public virtualAccountName?: string,
    public virtualAccountId?: number,
    public agent?: Agent,
    public gateway?: Gateway,
    public state?: StateEnum
  ) {}
}
