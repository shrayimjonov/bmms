import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';
import { DepositTransaction } from './deposit-transaction.model';
import { createRequestOption } from '../../../core/util/request-util';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { EntityViewOptions } from '../../../shared/entity/entity-view/entity-view-options.model';
import { EntityService } from '../../../shared/entity/entity.service';

type EntityResponseType = HttpResponse<DepositTransaction>;
type EntityArrayResponseType = HttpResponse<DepositTransaction[]>;

@Injectable({ providedIn: 'root' })
export class DepositTransactionService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/deposit-transactions', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(depositTransaction: DepositTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(depositTransaction);
    return this.http
      .post<DepositTransaction>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(depositTransaction: DepositTransaction): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(depositTransaction);
    return this.http
      .put<DepositTransaction>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<DepositTransaction>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<DepositTransaction[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(depositTransaction: DepositTransaction): DepositTransaction {
    const copy: DepositTransaction = Object.assign({}, depositTransaction, {
      updateDate:
        depositTransaction.updateDate && depositTransaction.updateDate.isValid() ? depositTransaction.updateDate.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.updateDate = res.body.updateDate ? moment(res.body.updateDate) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((depositTransaction: DepositTransaction) => {
        depositTransaction.updateDate = depositTransaction.updateDate ? moment(depositTransaction.updateDate) : undefined;
      });
    }
    return res;
  }

  openView(depositTransaction: DepositTransaction) {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: depositTransaction.id },
      { title: 'billMasterGwApp.pkbillmsDepositTransaction.payerTaxnr', value: depositTransaction.payerTaxnr },
      { title: 'billMasterGwApp.pkbillmsDepositTransaction.recipientTaxnr', value: depositTransaction.recipientTaxnr },
      { title: 'billMasterGwApp.pkbillmsDepositTransaction.amount', value: depositTransaction.amount },
      {
        title: 'billMasterGwApp.pkbillmsDepositTransaction.updateDate',
        value: depositTransaction.updateDate,
        type: 'date',
        date: 'dd.MM.yyyy HH:mm:ss',
      },
      { title: 'billMasterGwApp.pkbillmsDepositTransaction.comment', value: depositTransaction.comment },
      { title: 'billMasterGwApp.pkbillmsDepositTransaction.doc', value: depositTransaction.doc },
    ];
    this.entityService.view(options, depositTransaction.id);
  }
}
