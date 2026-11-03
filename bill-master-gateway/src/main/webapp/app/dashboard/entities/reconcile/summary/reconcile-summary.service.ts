import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../../core/util/request-util';
import {ApplicationConfigService} from '../../../../core/config/application-config.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityService} from '../../../../shared/entity/entity.service';
import {ReconcileManual, ReconcileSummary} from './reconcile-summary.model';
import {IEntityConfig} from '../../../../shared/entity/entity-config.model';
import {EntityViewOptions} from '../../../../shared/entity/entity-view/entity-view-options.model';
import {Result} from '../../../../shared/model/pkbillms/result.model';
import {EnumerationService} from '../../../../shared/service/enumeration/enumeration.service';
import {ReconcileSummaryStatus} from '../../../../shared/model/enumerations/reconcile-summary-status.model';
import {GatewayService} from '../../gateway/gateway.service';
import {Bill} from '../../bill/bill.model';
import {P2pReconcile} from "./p2p/p2p-reconcile.model";

type EntityResponseType = HttpResponse<ReconcileSummary>;
type EntityArrayResponseType = HttpResponse<ReconcileSummary[]>;

@Injectable({providedIn: 'root'})
export class ReconcileSummaryService implements IEntityConfig {
  public resourceUrl = this.appConfigService.getEndpointFor('api/reconcile-summaries', 'bmms');

  constructor(
    private readonly http: HttpClient,
    private readonly entityService: EntityService,
    private readonly enumService: EnumerationService,
    private readonly gatewayService: GatewayService,
    private readonly appConfigService: ApplicationConfigService
  ) {
  }

  create(reconcileSummary: ReconcileSummary): Observable<EntityResponseType> {
    return this.http.post<ReconcileSummary>(this.resourceUrl, reconcileSummary, {observe: 'response'});
  }

  update(reconcileSummary: ReconcileSummary): Observable<EntityResponseType> {
    return this.http.put<ReconcileSummary>(`${this.resourceUrl}/${reconcileSummary.id}`, reconcileSummary, {observe: 'response'});
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ReconcileSummary>(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ReconcileSummary[]>(this.resourceUrl, {params: options, observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  reconcileByCardType(p2pReconcile: P2pReconcile): Observable<boolean> {
    const apiSuffix = `api/reconcile/${p2pReconcile.type.toLowerCase()}/p2p/run`;
    return this.http.post<boolean>(this.appConfigService.getEndpointFor(apiSuffix, 'bmms'), p2pReconcile, {observe: 'body'});
  }

  manualReconcile(reconcileManual: ReconcileManual): Observable<Result> {
    return this.http.post<Result>(this.appConfigService.getEndpointFor('api/reconcile/manual', 'bmms'), reconcileManual, {
      observe: 'body',
    });
  }

  terminalReconcile(terminalId: string): Observable<Bill> {
    const req = {
      terminalId: terminalId,
    };

    return this.http.post<Result>(this.appConfigService.getEndpointFor('api/humo/reconcile/run/terminal', 'bmms'), req, {
      observe: 'body',
    });
  }

  getFilterFields(): NglFilterField[] {
    return [
      {
        name: 'date.greaterThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.fromDate',
      },
      {
        name: 'date.lessThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.toDate',
      },
      {
        name: 'gatewayName.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsReconcileSummary.gateway',
        options: {
          'select.resourceUrl': this.gatewayService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'name',
        },
      },
      {
        name: 'status.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsReconcileSummary.status',
        options: {
          'select.items': this.enumService.getOnlyValues('ReconcileSummaryStatus', ReconcileSummaryStatus),
        },
      },
      {
        name: 'success.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsReconcileSummary.success',
        options: {
          'select.nzLabelTranslation': 'billMasterGwApp.pkbillmsReconcileSummary.success',
          'select.items': [true, false],
        },
        value: false,
      },
    ];
  }

  openUpdate(): void {
  }

  openView(reconcileSummary: ReconcileSummary): void {
    const successContainer: HTMLButtonElement = document.createElement('button');
    successContainer.className = reconcileSummary.success ? 'btn-success' : 'btn-danger';
    successContainer.setAttribute('jhiTranslate', 'billMasterGwApp.pkbillmsReconcileSummary.success.' + reconcileSummary.success);
    successContainer.setAttribute('routerLink', '/dashboard/reconcile-discrepancy');
    successContainer.setAttribute('queryParams', `{reconcileSummaryId: ${reconcileSummary.id}}`);

    const options: EntityViewOptions[] = [
      {title: 'global.field.id', value: reconcileSummary.id},
      {title: 'billMasterGwApp.pkbillmsReconcileSummary.gatewayName', value: reconcileSummary.gatewayName},
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.date',
        value: reconcileSummary.date,
        type: 'date',
        date: 'dd.MM.yyyy HH:mm:ss',
      },
      {title: 'billMasterGwApp.pkbillmsReconcileSummary.systemCount', value: reconcileSummary.systemCount},
      {title: 'billMasterGwApp.pkbillmsReconcileSummary.gatewayCount', value: reconcileSummary.gatewayCount},
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.diffCount',
        value: reconcileSummary.systemCount - reconcileSummary.gatewayCount,
      },
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.systemAmount',
        value: reconcileSummary.systemAmount,
        type: 'amount',
      },
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.gatewayAmount',
        value: reconcileSummary.gatewayAmount,
        type: 'amount',
      },
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.diffAmount',
        value: reconcileSummary.systemAmount - reconcileSummary.gatewayAmount,
        type: 'amount',
      },
      {title: 'billMasterGwApp.pkbillmsReconcileSummary.status', value: successContainer},
      {
        title: 'billMasterGwApp.pkbillmsReconcileSummary.createdDate',
        value: reconcileSummary.createdDate,
        type: 'date',
        date: 'dd.MM.yyyy HH:mm:ss',
      },
    ];

    this.entityService.view(options, reconcileSummary.id);
  }

  openDelete(): void {
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'ReconcileSummary',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsReconcileSummary',
    };

    this.entityService.history(options);
  }
}
