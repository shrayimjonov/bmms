import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';
import { Bill } from './bill.model';
import { createRequestOption } from '../../../core/util/request-util';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { IEntityConfig } from '../../../shared/entity/entity-config.model';
import { EntityService } from '../../../shared/entity/entity.service';
import { EntityUpdateOptions } from '../../../shared/entity/entity-update/entity-update-options.model';
import { RecipientService } from '../recipient/recipient.service';
import { BillInfoModalComponent } from './bill-info-modal.component';
import { EnumerationService } from '../../../shared/service/enumeration/enumeration.service';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { RequestType } from '../../../shared/model/enumerations/request-type.model';

type EntityResponseType = HttpResponse<Bill>;
type EntityArrayResponseType = HttpResponse<Bill[]>;

@Injectable({ providedIn: 'root' })
export class BillService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/bills', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private recipientService: RecipientService,
    private enumerationService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(bill: Bill): Observable<EntityResponseType> {
    return this.http.post<Bill>(this.resourceUrl, bill, { observe: 'response' });
  }

  update(bill: Bill): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bill);
    return this.http
      .put<Bill>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<Bill>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<Bill[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(bill: Bill): Bill {
    const copy: Bill = Object.assign({}, bill, {
      requestTime: bill.requestTime && bill.requestTime.isValid() ? bill.requestTime.toJSON() : undefined,
      responseTime: bill.responseTime && bill.responseTime.isValid() ? bill.responseTime.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.requestTime = res.body.requestTime ? moment(res.body.requestTime) : undefined;
      res.body.responseTime = res.body.responseTime ? moment(res.body.responseTime) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((bill: Bill) => {
        bill.requestTime = bill.requestTime ? moment(bill.requestTime) : undefined;
        bill.responseTime = bill.responseTime ? moment(bill.responseTime) : undefined;
      });
    }
    return res;
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBill.recipientId',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      { name: 'account.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsBill.account' },
      { name: 'amount.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsBill.amount' },
      {
        name: 'success.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsBill.success',
        options: {
          'select.nzLabelTranslation': 'global.success',
          'select.items': [true, false],
        },
      },
      // { name: 'sessionId.equals', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsBill.sessionId' },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBill.type',
        options: {
          'select.items': this.enumerationService.getOnlyValues('RequestType', RequestType),
        },
      },
      {
        name: 'requestTime.equals',
        type: NglFilterFieldType.DATE,
        translation: 'billMasterGwApp.pkbillmsBill.requestTime',
        placeholder: 'global.form.date.placeholder',
      },
      {
        name: 'responseTime.equals',
        type: NglFilterFieldType.DATE,
        translation: 'billMasterGwApp.pkbillmsBill.responseTime',
        placeholder: 'global.form.date.placeholder',
      },
      { name: 'login.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsBill.login' },
    ];
  }

  openUpdate(): void {}

  openView(bill: Bill): void {
    const options: EntityUpdateOptions<BillInfoModalComponent> = {
      component: BillInfoModalComponent,
      componentInstances: {
        bill: bill,
      },
    };

    this.entityService.update(options);
  }

  openDelete(): void {}

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Bill',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsBill',
    };

    this.entityService.history(options);
  }
}
