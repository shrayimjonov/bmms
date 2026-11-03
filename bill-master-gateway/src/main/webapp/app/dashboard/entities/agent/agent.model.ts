import {I18n} from '../../../shared/model/pkbillms/i18n.model';
import {Moment} from 'moment';
import {StateEnum} from '../user-management/user/user.model';

export class DetailedAgent {
  constructor(public password?: string,
              public email?: string,
              public agent?: Agent,
              public agentDTO?: Agent,
              public id?: number) {}
}

export class Agent {
  constructor(
    public id?: number,
    public shortName?: string,
    public amountRatio?: number,
    public apiKey?: string,
    public currencies?: string,
    public ipAddresses?: string,
    public logoContentType?: string,
    public logo?: any,
    public ord?: number,
    public active?: boolean,
    public phoneNumber?: string,
    public inn?: string,
    public orgName?: string,
    public titles?: I18n[],
    public depositId?: number,
    public email?: string,
    public bankId?: number,
    public generalContractNumber?: number,
    public contractDate?: Moment,
    public state?: StateEnum
  ) {}
}
