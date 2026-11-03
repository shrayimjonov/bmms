import {I18n} from '../../../shared/model/pkbillms/i18n.model';

export class HomeWidgetStat {
  amount?: any;
  percent?: number;
}

export class HomeWidgetTop {
  id?: number;
  name?: string;
  amount?: number;
  count?: number;
}

export class HomeWidget<T extends HomeWidgetStat | HomeWidgetTop[] = any> {
  titles: I18n[];
  type: 'STATISTIC' | 'GATEWAY' | 'RECIPIENT';
  data: T;
}

export enum DepositWidgetType {
  PROFIT = 'PROFIT',
  TURNOVER = 'TURNOVER',
  BALANCE = 'BALANCE'
}

export class UserHomeWidget {
  titles: I18n[];
  type: DepositWidgetType;
  amount: number;
  percent: number;
}

