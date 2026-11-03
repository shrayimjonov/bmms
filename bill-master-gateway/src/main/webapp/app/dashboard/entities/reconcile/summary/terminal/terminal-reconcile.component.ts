import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {pluck} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';
import {BmAlertService} from '../../../../../shared/component/bm-alert/bm-alert.service';
import * as moment from 'moment';
import {EntityBaseComponent} from '../../../../../shared/entity/entity-base-component';
import {LanguageService} from '../../../../../core/util/language.service';
import {NglFilterFieldType} from 'ngl-filter-field';
import {Event} from "../../../../../config/event.constants";
import {ReconcileSummaryService} from "../reconcile-summary.service";
import {ReconcileSummary, ReconcileType} from "../reconcile-summary.model";

@Component({
  templateUrl: 'terminal-reconcile.template.html',
  styleUrls: ['../../reconcile-status.style.scss'],
})
export class TerminalReconcileComponent extends EntityBaseComponent<ReconcileSummary> implements OnInit, OnDestroy {
  constructor(public readonly reconcileSummaryService: ReconcileSummaryService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly langService: LanguageService,
              private readonly alertService: BmAlertService) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute);

    this.filterFields = this.reconcileSummaryService.getFilterFields();
    this.filterFields.forEach(field => {
      if (field.name === 'gatewayName.in') {
        field.name = 'gatewayName.contains';
        field.type = NglFilterFieldType.TEXT;
        field.translation = 'billMasterGwApp.pkbillmsReconcileSummary.terminalId';
        field.options = null;
      }
    });

    this.registerToModify(Event.RECONCILE_SUMMARY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = {
      ...this.getRequestParams(page),
      ['type.equals']: ReconcileType.TERMINAL
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

  reconcile(terminalId: string): void {
    this.loading = true;
    this.reconcileSummaryService.terminalReconcile(terminalId)
      .pipe(
        pluck('success'),
        this.loadPageOperators()
      )
      .subscribe(
        (success: boolean) => this.onReconciled(success),
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
