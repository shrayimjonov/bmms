import {Component, OnDestroy,} from '@angular/core';
import {Subject} from "rxjs";
import {HomeWidget} from "./home-widget.model";
import {Period} from "../../../shared/model/period.model";
import * as moment from "moment";
import {Moment} from "moment";
import {HomeWidgetService} from "./home-widget.service";
import {GatewayService} from "../gateway/gateway.service";
import {RecipientService} from "../recipient/recipient.service";
import {BmAlertService} from "../../../shared/component/bm-alert/bm-alert.service";
import {finalize, takeUntil} from "rxjs/operators";
import {HttpResponse} from "@angular/common/http";
import {Gateway} from "../gateway/gateway.model";
import {Recipient} from "../recipient/recipient.model";

@Component({
  templateUrl: 'home.template.html',
  styleUrls: ['home.style.scss'],
})
export class HomeComponent implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  widgets: HomeWidget[];
  loading: boolean;
  period: Period = Period.WEEK;

  readonly periods = Object.keys(Period).filter(p => Period[p] !== Period.ALL).map(period => Period[period]);

  private readonly periodToDate = new Map<Period, Moment>([
    [Period.TODAY, moment().utc().hours(0)],
    [Period.WEEK, moment().utc().startOf('isoWeek')],
    [Period.MONTH, moment().utc().startOf('month')],
  ]);

  constructor(
    private readonly homeWidgetService: HomeWidgetService,
    private readonly gatewayService: GatewayService,
    private readonly recipientService: RecipientService,
    private readonly alertService: BmAlertService
  ) {
  }

  viewGateway(id: number): void {
    this.gatewayService
      .find(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((res: HttpResponse<Gateway>) => this.gatewayService.openView(res.body));
  }

  viewRecipient(id: number): void {
    this.recipientService
      .find(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((res: HttpResponse<Recipient>) => this.recipientService.openView(res.body));
  }


  filter(period: Period, force?: boolean): void {
    const from = this.periodToDate.get(period).toJSON();
    this.loading = true;

    this.homeWidgetService
      .filter(from, force)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((items: HomeWidget[]) => {
        this.widgets = items;

        if (!items?.length) {
          this.alertService.warning('global.data.notFound');
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
