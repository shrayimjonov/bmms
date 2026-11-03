import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import * as moment from 'moment';
import {createRequestOption} from '../../../core/util/request-util';
import {Transaction, TransactionStatus} from './transaction.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {AgentService} from '../agent/agent.service';
import {RecipientService} from '../recipient/recipient.service';
import {TransactionUpdateModalComponent} from './transaction-update-modal.component';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {Action} from '../../../shared/model/action.model';
import {HttpMethod} from '../../../admin/metrics/metrics.model';

type EntityResponseType = HttpResponse<Transaction>;
type EntityArrayResponseType = HttpResponse<Transaction[]>;

@Injectable({providedIn: 'root'})
export class TransactionService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/transactions', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private agentService: AgentService,
    private recipientService: RecipientService,
    private enumerationService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {
  }

  create(transaction: Transaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(transaction);
    return this.http
      .post<Transaction>(this.resourceUrl, copy, {observe: 'response'})
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(transaction: Transaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(transaction);
    return this.http
      .put<Transaction>(`${this.resourceUrl}/${transaction.id}`, copy, {observe: 'response'})
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<Transaction>(`${this.resourceUrl}/${id}`, {observe: 'response'})
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<Transaction[]>(this.resourceUrl, {params: options, observe: 'response'})
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  getActions(status: TransactionStatus): Observable<Action[]> {
    return this.http.get<Action[]>(`${this.resourceUrl}/actions/${status}`, {observe: 'body'});
  }

  executeAction(url: string, method: HttpMethod, body?: any): Observable<any> {
    const serverApi = `${this.applicationConfigService.getEndpointFor(url, 'bmms')}`;

    return this.http.request<any>(method, serverApi, {
      body: body,
      observe: 'body',
    });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  protected convertDateFromClient(transaction: Transaction): Transaction {
    const copy: Transaction = Object.assign({}, transaction, {
      senderDate: transaction.senderDate && transaction.senderDate.isValid() ? transaction.senderDate.toJSON() : undefined,
      recipientDate: transaction.recipientDate && transaction.recipientDate.isValid() ? transaction.recipientDate.toJSON() : undefined,
      date: transaction.date && transaction.date.isValid() ? transaction.date.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.senderDate = res.body.senderDate ? moment(res.body.senderDate) : undefined;
      res.body.recipientDate = res.body.recipientDate ? moment(res.body.recipientDate) : undefined;
      res.body.date = res.body.date ? moment(res.body.date) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((transaction: Transaction) => {
        transaction.senderDate = transaction.senderDate ? moment(transaction.senderDate) : undefined;
        transaction.recipientDate = transaction.recipientDate ? moment(transaction.recipientDate) : undefined;
        transaction.date = transaction.date ? moment(transaction.date) : undefined;
      });
    }
    return res;
  }

  getFilterFields(): NglFilterField[] {
    return [
      {name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id'},
      {
        name: 'date.greaterThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.fromDate',
        placeholder: 'global.form.date.placeholder',
        options: {
          'date.nzShowTime': true,
          'date.nzFormat': 'dd.MM.yy HH:mm:ss',
        },
      },
      {
        name: 'date.lessThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.toDate',
        placeholder: 'global.form.date.placeholder',
        options: {
          'date.nzShowTime': true,
          'date.nzFormat': 'dd.MM.yy HH:mm:ss',
        },
      },
      {
        name: 'senderAccount.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsTransaction.senderAccount',
      },
      {
        name: 'recipientAccount.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsTransaction.recipientAccount',
      },
      {
        name: 'recipientName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsTransaction.recipientName'
      },
      {
        name: 'requestLogin.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsTransaction.requestLogin',
        options: {
          'select.nzLabel': 'shortName',
          'select.nzValue': 'shortName',
          'select.resourceUrl': this.agentService.resourceUrl,
        },
      },
      {
        name: 'status.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsTransaction.status',
        options: {
          'select.items': this.enumerationService.getOnlyValues('TransactionStatus', TransactionStatus),
          'select.nzOptionOverflowSize': 15,
        },
      },
      {
        name: 'category.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipient.category',
      },
      {
        name: 'requestAmount.equals',
        type: NglFilterFieldType.AMOUNT,
        translation: 'billMasterGwApp.pkbillmsTransaction.requestAmount',
        options: {
          'amount.ratio': 100,
        }
      }
    ];
  }

  openUpdate(transaction?: Transaction): void {
    const options: EntityUpdateOptions<TransactionUpdateModalComponent> = {
      component: TransactionUpdateModalComponent,
      componentInstances: {
        transaction: transaction || {},
      },
      modalOptions: {size: 'lg'},
    };

    this.entityService.update(options);
  }

  openView(): void {
  }

  openDelete(): void {
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Transaction',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsTransaction',
    };

    this.entityService.history(options);
  }
}
