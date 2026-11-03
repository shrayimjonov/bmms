import { CashBackStatus, CashBackType } from '../../../shared/model/enumerations/cash-back-type.model';

export interface ICashBack {
  id?: number;
  bankCommission?: number;
  clientCommission?: number;
  name?: string | null;
  type?: CashBackType | null;
  status?: CashBackStatus | null;
  jsonParams?: any;
}

export class CashBack implements ICashBack {
  constructor(
    public id?: number,
    public bankCommission?: number,
    public clientCommission?: number,
    public name?: string | null,
    public type?: CashBackType | null,
    public status?: CashBackStatus | null,
    public jsonParams?: any
  ) {}
}

export function getCashBackIdentifier(cashBack: ICashBack): number | undefined {
  return cashBack.id;
}
