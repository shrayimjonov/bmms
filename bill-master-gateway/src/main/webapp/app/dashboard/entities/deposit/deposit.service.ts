import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Deposit} from './deposit.model';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {ReplenishDeposit} from './replenish-deposit.model';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {EntityService} from '../../../shared/entity/entity.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {DepositUpdateModalComponent} from './deposit-update-modal.component';
import {GatewayService} from '../gateway/gateway.service';
import {AgentService} from '../agent/agent.service';
import {ReplenishDepositModalComponent} from './replenish-deposit-modal.component';

type EntityResponseType = HttpResponse<Deposit>;
type EntityArrayResponseType = HttpResponse<Deposit[]>;

@Injectable({ providedIn: 'root' })
export class DepositService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/deposits', 'bmms');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private gatewayService: GatewayService,
    private agentService: AgentService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(deposit: Deposit): Observable<EntityResponseType> {
    return this.http.post<Deposit>(this.resourceUrl, deposit, { observe: 'response' });
  }

  update(deposit: Deposit): Observable<EntityResponseType> {
    return this.http.put<Deposit>(`${this.resourceUrl}/${deposit.id}`, deposit, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<Deposit>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<Deposit[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  depositReplenish(replenishDeposit: ReplenishDeposit): Observable<HttpResponse<any>> {
    return this.http.post<any>(this.resourceUrl + '/replenish', replenishDeposit, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'agentId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsDeposit.agent',
        options: {
          'select.resourceUrl': this.agentService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'overdraftLimit.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsDeposit.overdraftLimit',
      },
      { name: 'balance.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsDeposit.balance' },
      {
        name: 'balanceCcy.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsDeposit.balanceCcy',
      },
      // {
      //   name: 'bookedBalance.equals',
      //   type: NglFilterFieldType.NUMBER,
      //   translation: 'billMasterGwApp.pkbillmsDeposit.bookedBalance',
      // },
      // {
      //   name: 'limitInform.equals',
      //   type: NglFilterFieldType.NUMBER,
      //   translation: 'billMasterGwApp.pkbillmsDeposit.limitInform',
      // },
      // {
      //   name: 'limitDisable.equals',
      //   type: NglFilterFieldType.NUMBER,
      //   translation: 'billMasterGwApp.pkbillmsDeposit.limitDisable',
      // },
      // {
      //   name: 'gatewayId.in',
      //   type: NglFilterFieldType.MULTI_SELECT,
      //   translation: 'billMasterGwApp.pkbillmsDeposit.gateway',
      //   options: {
      //     'select.resourceUrl': this.gatewayService.resourceUrl,
      //     'select.nzLabel': 'name',
      //     'select.nzValue': 'id',
      //   },
      // },
    ];
  }

  openUpdate(deposit?: Deposit, duplicate?: boolean): void {
    const options: EntityUpdateOptions<DepositUpdateModalComponent> = {
      component: DepositUpdateModalComponent,
      componentInstances: {
        data: deposit || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(deposit: Deposit): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: deposit.id },
      { title: 'billMasterGwApp.pkbillmsDeposit.overdraftLimit', value: deposit.overdraftLimit },
      { title: 'billMasterGwApp.pkbillmsDeposit.balance', value: deposit.balance },
      { title: 'billMasterGwApp.pkbillmsDeposit.balanceCcy', value: deposit.balanceCcy },
      { title: 'billMasterGwApp.pkbillmsDeposit.blocked', value: deposit.blocked },
      { title: 'billMasterGwApp.pkbillmsDeposit.unlimit', value: deposit.unlimit },
      {
        title: 'billMasterGwApp.pkbillmsDeposit.gateway',
        value: deposit.gateway?.name,
        type: 'link',
        link: () => this.gatewayService.openView(deposit.gateway),
      },
      {
        title: 'billMasterGwApp.pkbillmsDeposit.agent',
        value: deposit.agent?.shortName,
        type: 'link',
        link: () => this.agentService.openView(deposit.agent),
      },
    ];

    this.entityService.view(options, deposit.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.DEPOSIT_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsDeposit.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Deposit',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsDeposit',
    };

    this.entityService.history(options);
  }

  openReplenish(deposit: Deposit): void {
    const options: EntityUpdateOptions<ReplenishDepositModalComponent> = {
      component: ReplenishDepositModalComponent,
      componentInstances: {
        deposit: deposit || {},
      },
      modalOptions: { backdrop: 'static', size: 'lg' },
    };

    this.entityService.update<ReplenishDepositModalComponent>(options);
  }
}
