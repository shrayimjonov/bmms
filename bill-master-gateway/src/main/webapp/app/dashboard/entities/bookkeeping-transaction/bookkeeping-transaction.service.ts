import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';
import { BookkeepingTransaction } from '../../../shared/model/pkbillms/bookkeeping-transaction.model';
import { createRequestOption } from '../../../core/util/request-util';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { IEntityConfig } from '../../../shared/entity/entity-config.model';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { EntityViewOptions } from '../../../shared/entity/entity-view/entity-view-options.model';
import { EntityService } from '../../../shared/entity/entity.service';
import { EnumerationService } from '../../../shared/service/enumeration/enumeration.service';
import { BookkeepingTransactionStatus } from '../../../shared/model/enumerations/bookkeeping-transaction-status.model';
import { GatewayService } from '../gateway/gateway.service';
import { RecipientService } from '../recipient/recipient.service';

type EntityResponseType = HttpResponse<BookkeepingTransaction>;
type EntityArrayResponseType = HttpResponse<BookkeepingTransaction[]>;

@Injectable({ providedIn: 'root' })
export class BookkeepingTransactionService implements IEntityConfig {
  public resourceUrl = this.appConfigService.getEndpointFor('api/bookkeeping-transactions', 'bmms');

  constructor(
    private readonly http: HttpClient,
    private readonly entityService: EntityService,
    private readonly enumService: EnumerationService,
    private readonly gatewayService: GatewayService,
    private readonly recipientService: RecipientService,
    private readonly appConfigService: ApplicationConfigService
  ) {}

  create(bookkeepingTransaction: BookkeepingTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bookkeepingTransaction);
    return this.http
      .post<BookkeepingTransaction>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(bookkeepingTransaction: BookkeepingTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bookkeepingTransaction);
    return this.http
      .put<BookkeepingTransaction>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<BookkeepingTransaction>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<BookkeepingTransaction[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(bookkeepingTransaction: BookkeepingTransaction): BookkeepingTransaction {
    const copy: BookkeepingTransaction = Object.assign({}, bookkeepingTransaction, {
      createTime:
        bookkeepingTransaction.createTime && bookkeepingTransaction.createTime.isValid()
          ? bookkeepingTransaction.createTime.toJSON()
          : undefined,
      confirmTime:
        bookkeepingTransaction.confirmTime && bookkeepingTransaction.confirmTime.isValid()
          ? bookkeepingTransaction.confirmTime.toJSON()
          : undefined,
      sendTime:
        bookkeepingTransaction.sendTime && bookkeepingTransaction.sendTime.isValid() ? bookkeepingTransaction.sendTime.toJSON() : undefined,
      cancelTime:
        bookkeepingTransaction.cancelTime && bookkeepingTransaction.cancelTime.isValid()
          ? bookkeepingTransaction.cancelTime.toJSON()
          : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createTime = res.body.createTime ? moment(res.body.createTime) : undefined;
      res.body.confirmTime = res.body.confirmTime ? moment(res.body.confirmTime) : undefined;
      res.body.sendTime = res.body.sendTime ? moment(res.body.sendTime) : undefined;
      res.body.cancelTime = res.body.cancelTime ? moment(res.body.cancelTime) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((bookkeepingTransaction: BookkeepingTransaction) => {
        bookkeepingTransaction.createTime = bookkeepingTransaction.createTime ? moment(bookkeepingTransaction.createTime) : undefined;
        bookkeepingTransaction.confirmTime = bookkeepingTransaction.confirmTime ? moment(bookkeepingTransaction.confirmTime) : undefined;
        bookkeepingTransaction.sendTime = bookkeepingTransaction.sendTime ? moment(bookkeepingTransaction.sendTime) : undefined;
        bookkeepingTransaction.cancelTime = bookkeepingTransaction.cancelTime ? moment(bookkeepingTransaction.cancelTime) : undefined;
      });
    }
    return res;
  }

  getFilterFields(): NglFilterField[] {
    return [
      {
        name: 'createTime.greaterThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.fromDate',
      },
      {
        name: 'createTime.lessThanOrEqual',
        type: NglFilterFieldType.DATE,
        translation: 'global.field.toDate',
      },
      {
        name: 'sAccount.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTransaction.sAccount',
      },
      {
        name: 'rAccount.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTransaction.rAccount',
      },
      {
        name: 'gatewayId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'global.menu.entities.pkbillmsGateway',
        options: {
          'select.resourceUrl': this.gatewayService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'global.menu.entities.pkbillmsRecipient',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTransaction.type',
        options: {
          'select.items': this.enumService.getOnlyValues('BookkeepingTransactionStatus', BookkeepingTransactionStatus),
        },
      },
    ];
  }

  openUpdate(): void {}

  openView(bookkeepingTransaction: BookkeepingTransaction): void {
    const transactionStatusContainer: HTMLSpanElement = document.createElement('span');
    transactionStatusContainer.className = 'badge bookkeeping-transaction-status' + bookkeepingTransaction.type;
    transactionStatusContainer.setAttribute('jhiTranslate', 'billMasterGwApp.BookkeepingTransactionStatus.' + bookkeepingTransaction.type);

    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: bookkeepingTransaction.id },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.type', value: transactionStatusContainer },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.recipientShortName',
        value: bookkeepingTransaction.recipientShortName,
      },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.gatewayName', value: bookkeepingTransaction.gatewayName },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.sMfo', value: bookkeepingTransaction.sMfo },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.rMfo', value: bookkeepingTransaction.rMfo },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.sAccount', value: bookkeepingTransaction.sAccount },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.rAccount', value: bookkeepingTransaction.rAccount },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.amount',
        value: bookkeepingTransaction.amount,
        type: 'amount',
      },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.createTime',
        value: bookkeepingTransaction.createTime,
        type: 'date',
        date: 'dd.MM.yyyy HH:mm:ss',
      },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.confirmTime',
        value: bookkeepingTransaction.confirmTime,
        type: 'date',
        date: 'dd.MM.yyyy HH:mm:ss',
      },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTransaction.sendUserID', value: bookkeepingTransaction.sendUserID },
    ];
    this.entityService.view(options, bookkeepingTransaction.id);
  }

  openDelete(): void {}

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'BookkeepingTransaction',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsBookkeepingTransaction',
    };

    this.entityService.history(options);
  }
}
