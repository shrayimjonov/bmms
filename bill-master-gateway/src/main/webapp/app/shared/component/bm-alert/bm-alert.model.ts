export type BmAlertType = 'success' | 'info' | 'warning' | 'error';

export class BmAlert {
  constructor(public type?: BmAlertType,
              public message?: string,
              public params?: any,
              public timeout?: number,
              public isToast?: boolean) {
  }
}
