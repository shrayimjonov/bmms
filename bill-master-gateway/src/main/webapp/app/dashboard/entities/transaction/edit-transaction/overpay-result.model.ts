export enum RequestType {
  CHECK = 'CHECK',
  PAY = 'PAY',
  STATUS = 'STATUS',
  CANCEL = 'CANCEL',
  DEPOSIT = 'DEPOSIT',
  CREDIT = 'CREDIT',
  DEBIT = 'DEBIT',
  RECONCILE = 'RECONCILE'
}

export class OverpayResult {
  constructor(public success?: boolean,
              public resultText?: string,
              public requestType?: RequestType) {
  }
}
