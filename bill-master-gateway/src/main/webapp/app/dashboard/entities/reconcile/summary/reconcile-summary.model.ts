import {ReconcileSummaryStatus} from '../../../../shared/model/enumerations/reconcile-summary-status.model';
import {ReconcilePeriod} from '../../../../shared/model/enumerations/reconcile-period.model';

export enum P2PGatewayType {
  UZCARD_P2P_DEBIT_CONFIRM = 'UZCARD_P2P_DEBIT_CONFIRM',
  UZCARD_P2P_CREDIT_CONFIRM = 'UZCARD_P2P_CREDIT_CONFIRM',
  HUMO_P2P_DEBIT_CONFIRM = 'HUMO_P2P_DEBIT_CONFIRM',
  HUMO_P2P_CREDIT_CONFIRM = 'HUMO_P2P_CREDIT_CONFIRM',
  VISA_P2P_DEBIT_CONFIRM = 'VISA_P2P_DEBIT_CONFIRM',
  VISA_P2P_CREDIT_CONFIRM = 'VISA_P2P_CREDIT_CONFIRM',
  NCI_P2P_DEBIT_CONFIRM = 'NCI_P2P_DEBIT_CONFIRM',
  NCI_P2P_CREDIT_CONFIRM = 'NCI_P2P_CREDIT_CONFIRM',
  FOREX_P2P_DEBIT_CONFIRM = 'FOREX_P2P_DEBIT_CONFIRM',
  FOREX_P2P_CREDIT_CONFIRM = 'FOREX_P2P_CREDIT_CONFIRM',
  ANOR_P2P_DEBIT_CONFIRM = 'ANOR_P2P_DEBIT_CONFIRM',
  ANOR_P2P_CREDIT_CONFIRM = 'ANOR_P2P_CREDIT_CONFIRM',
  TKB_P2P_DEBIT_CONFIRM = 'TKB_P2P_DEBIT_CONFIRM',
  TKB_P2P_CREDIT_CONFIRM = 'TKB_P2P_CREDIT_CONFIRM'
}

export enum CardType {
  UZCARD = 'UZCARD',
  HUMO = 'HUMO'
}

export enum ReconcileType {
  PAYMENT = 'PAYMENT',
  TERMINAL = 'TERMINAL',
  P2P = 'P2P'
}

export class ReconcileManual {
  gatewayService?: string;
  localDate?: Date;
}

export class ReconcileSummary {
  constructor(
    public id?: number,
    public gatewayId?: number,
    public systemAmount?: number,
    public gatewayAmount?: number,
    public systemCount?: number,
    public gatewayCount?: number,
    public date?: Date,
    public status?: ReconcileSummaryStatus,
    public success?: boolean,
    public period?: ReconcilePeriod,
    public gatewayName?: string,
    public createdDate?: Date,
    public type?: ReconcileType
  ) {
  }
}
