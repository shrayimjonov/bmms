import {Injectable} from '@angular/core';
import {BmAlert} from 'app/shared/component/bm-alert/bm-alert.model';
import {Observable, ReplaySubject} from 'rxjs';
import {NzMessageService} from 'ng-zorro-antd/message';
import {getTranslation} from "../../functions";

@Injectable({providedIn: 'root'})
export class BmAlertService {
  private alert = new ReplaySubject<BmAlert>();

  constructor(private readonly toastService: NzMessageService) {
  }

  add(alert: BmAlert): void {
    alert.params = alert.params instanceof Object ? alert.params : {param: alert.params};
    alert.message = getTranslation(alert.message, alert.params);

    if (alert.isToast) {
      this.toastService.create(alert.type, alert.message, {nzDuration: alert.timeout || 4000});
    } else {
      this.alert.next(alert);
    }

    if (alert.timeout) {
      setTimeout(() => this.clear(), alert.timeout);
    }
  }

  success(msg: string, params?: any, isToast?: boolean): void {
    const alert: BmAlert = {
      type: 'success',
      message: msg,
      params: params,
      isToast: isToast,
    };

    this.add(alert);
  }

  info(msg: string, params?: any, isToast?: boolean): void {
    const alert: BmAlert = {
      type: 'info',
      message: msg,
      params: params,
      isToast: isToast,
    };

    this.add(alert);
  }

  warning(msg: string, params?: any, isToast?: boolean): void {
    const alert: BmAlert = {
      type: 'warning',
      message: msg,
      params: params,
      isToast: isToast,
    };

    this.add(alert);
  }

  error(msg: string, params?: any, isToast?: boolean, timeout?: number): void {
    const alert: BmAlert = {
      type: 'error',
      message: msg,
      params: params,
      isToast: isToast,
      timeout: timeout,
    };

    this.add(alert);
  }

  get(): Observable<BmAlert> {
    return this.alert.asObservable();
  }

  clear(): void {
    this.alert.next(null);
  }
}
