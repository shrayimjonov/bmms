export enum BinPcType {
  UZCARD = 'UZCARD',
  HUMO = 'HUMO',
  VISA = 'VISA',
  NCI = 'NCI',
  FOREX = 'FOREX',
  ANOR = 'ANOR',
  TKB = 'TKB',
  TINKOFF = 'TINKOFF',
  MASTER_CARD = 'MASTER_CARD',
  MIR = 'MIR',
  UNION_PAY = 'UNION_PAY',
  WALLET = 'WALLET',
  WALLET_UZS = 'WALLET_UZS',
  WALLET_USD = 'WALLET_USD',
  WALLET_RUB = 'WALLET_RUB',
  OTHER = 'OTHER',
}

export class HumoBin {
  constructor(
    public id?: number,
    public bin?: string,
    public bankName?: string | null,
    public pcType?: BinPcType | null,
    public useCredit?: boolean,
    public useDebit?: boolean,
    public useReverse?: boolean,
    public useReconcile?: boolean,
    public comment?: string | null
  ) {}
}

export function getPcBinIdentifier(humoBin: HumoBin): number | undefined {
  return humoBin.id;
}
