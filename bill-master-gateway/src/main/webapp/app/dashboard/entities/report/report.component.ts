import {Component, OnDestroy, OnInit} from '@angular/core';
import {Report} from './report.model';
import {ReportService} from './report.service';
import {takeUntil} from 'rxjs/operators';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Agent} from '../agent/agent.model';
import {EntityBaseComponent} from "../../../shared/entity/entity-base-component";
import {FormBuilder} from "@angular/forms";
import {AgentService} from "../agent/agent.service";
import {RecipientService} from "../recipient/recipient.service";
import {EnumerationService} from "../../../shared/service/enumeration/enumeration.service";
import {PayType} from "../../../shared/model/enumerations/pay-type.model";
import {GatewayService} from "../gateway/gateway.service";
import * as moment from "moment";

@Component({
  templateUrl: 'report.template.html',
})
export class ReportComponent extends EntityBaseComponent<Report> implements OnInit, OnDestroy {
  payTypes: any[];

  readonly filterForm = this.fb.group({
    startDate: [null],
    endDate: [null],
    payTypes: [null],
    agents: [null],
    recipients: [null],
    gateways: [null]
  });

  private readonly DATE_FORMAT = 'YYYY-MM-DD HH:mm:ss';

  constructor(public readonly reportService: ReportService,
              public readonly agentService: AgentService,
              public readonly gatewayService: GatewayService,
              public readonly recipientService: RecipientService,
              private readonly fb: FormBuilder,
              private readonly activatedRoute: ActivatedRoute,
              private readonly enumService: EnumerationService) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute, () => this.loadPage());
    this.loadPayTypes();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPayTypes(): void {
    this.enumService.getOnlyValues('PayType', PayType)
      .pipe(
        takeUntil(this.destroy$)
      )
      .subscribe(payTypes => this.payTypes = payTypes);
  }

  download(id: number) {
    const criteria = this.filterForm.getRawValue();
    let params = '';

    if (this.filterForm.get('startDate')?.value) {
      criteria['startDate'] = moment(this.filterForm.get('startDate')?.value).format(this.DATE_FORMAT);
    }

    if (this.filterForm.get('endDate')?.value) {
      criteria['endDate'] = moment().format(this.DATE_FORMAT);
    }

    Object.keys(criteria).forEach(field => {
      if (criteria[field]) {
        params += '&criteria.' + field + '=' + criteria[field];
      }
    });

    window.open(`/services/reportms/download/report?id=${id}&format=xlsx${params}`, '_blank');
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.reportService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      ).subscribe((response: HttpResponse<Agent[]>) => this.onDataReceived(response));
  }
}
