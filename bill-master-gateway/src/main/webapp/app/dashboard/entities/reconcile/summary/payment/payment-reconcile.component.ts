import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {BmAlertService} from '../../../../../shared/component/bm-alert/bm-alert.service';
import * as moment from 'moment';
import {EntityBaseComponent} from '../../../../../shared/entity/entity-base-component';
import {LanguageService} from '../../../../../core/util/language.service';
import {ReconcileManual, ReconcileSummary, ReconcileType} from "../reconcile-summary.model";
import {Event} from "../../../../../config/event.constants";
import {ReconcileSummaryService} from "../reconcile-summary.service";

@Component({
  templateUrl: 'payment-reconcile.template.html',
  styleUrls: ['../../reconcile-status.style.scss'],
})
export class PaymentReconcileComponent extends EntityBaseComponent<ReconcileSummary> implements OnInit, OnDestroy {
  constructor(public readonly reconcileSummaryService: ReconcileSummaryService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly langService: LanguageService,
              private readonly alertService: BmAlertService) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute);
    this.filterFields = this.reconcileSummaryService.getFilterFields();

    this.registerToModify(Event.RECONCILE_SUMMARY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = {
      ...this.getRequestParams(page),
      ['type.equals']: ReconcileType.PAYMENT
    };

    this.loading = true;
    this.reconcileSummaryService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<ReconcileSummary[]>) => this.onDataReceived(response),);
  }

  filter(query: any): void {
    const from = query['date.greaterThanOrEqual'];
    const to = query['date.lessThanOrEqual'];

    if (from) {
      query['date.greaterThanOrEqual'] = moment(from).format('YYYY-MM-DD')
    }

    if (to) {
      query['date.lessThanOrEqual'] = moment(to).format('YYYY-MM-DD');
    }

    this.query = query;
    this.loadPage();
  }

  reconcile(reconcileSummary: ReconcileSummary): void {
    const req: ReconcileManual = {
      gatewayService: reconcileSummary.gatewayName,
      localDate: reconcileSummary.date
    };

    this.loading = true;
    this.reconcileSummaryService
      .manualReconcile(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe(
        (result: any) => this.onReconciled(result.success, result?.exception),
        error => this.alertService.error(error.error.title, null, true)
      );
  }

  private onReconciled(success: boolean, exception?: any): void {
    if (!success) {
      exception = exception || 'billMasterGwApp.pkbillmsReconcileSummary.reconcile.error';
      return this.alertService.error(exception[this.langService.getCurrent()], null, true);
    }

    this.alertService.success('billMasterGwApp.pkbillmsReconcileSummary.reconcile.success', null, true);
    this.loadPage();
  }
}
