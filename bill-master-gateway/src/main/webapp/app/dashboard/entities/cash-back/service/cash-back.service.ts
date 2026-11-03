import { Injectable, isDevMode } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IEntityConfig } from 'app/shared/entity/entity-config.model';
import { EntityService } from 'app/shared/entity/entity.service';
import { EntityUpdateOptions } from 'app/shared/entity/entity-update/entity-update-options.model';
import { EntityViewOptions } from 'app/shared/entity/entity-view/entity-view-options.model';
import { CashBackUpdateComponent } from '../update/cash-back-update.component';
import { ICashBack, getCashBackIdentifier } from '../cash-back.model';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { Event } from '../../../../config/event.constants';
import { createRequestOption } from '../../../../core/util/request-util';
import { CashBackStatus, CashBackType } from '../../../../shared/model/enumerations/cash-back-type.model';

export type EntityResponseType = HttpResponse<ICashBack>;
export type EntityArrayResponseType = HttpResponse<ICashBack[]>;

@Injectable({ providedIn: 'root' })
export class CashBackService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/cash-backs', 'bmms');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
    private entityService: EntityService
  ) {}

  create(cashBack: ICashBack): Observable<EntityResponseType> {
    return this.http.post<ICashBack>(this.resourceUrl, cashBack, { observe: 'response' });
  }

  update(cashBack: ICashBack): Observable<EntityResponseType> {
    return this.http.put<ICashBack>(`${this.resourceUrl}/${getCashBackIdentifier(cashBack) as number}`, cashBack, { observe: 'response' });
  }

  partialUpdate(cashBack: ICashBack): Observable<EntityResponseType> {
    return this.http.patch<ICashBack>(`${this.resourceUrl}/${getCashBackIdentifier(cashBack) as number}`, cashBack, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICashBack>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return isDevMode()
      ? of(
          new HttpResponse({
            body: [
              {
                id: 1,
                bankCommission: 1,
                clientCommission: 2,
                name: 'name',
                type: CashBackType.AMOUNT,
                status: CashBackStatus.ACTIVE,
                jsonParams: '{}',
              },
            ],
          })
        )
      : this.http.get<ICashBack[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      {
        name: 'id.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsCashBack.id',
      },
      {
        name: 'bankCommission.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsCashBack.bankCommission',
      },
      {
        name: 'clientCommission.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsCashBack.clientCommission',
      },
      {
        name: 'name.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsCashBack.name',
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsCashBack.type',
        options: {
          'select.items': Object.keys(CashBackType),
        },
      },
      {
        name: 'status.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsCashBack.status',
        options: {
          'select.items': Object.keys(CashBackStatus),
        },
      },
    ];
  }

  openUpdate(cashBack?: ICashBack): void {
    const options: EntityUpdateOptions<CashBackUpdateComponent> = {
      component: CashBackUpdateComponent,
      componentInstances: {
        data: cashBack || {},
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(entity: ICashBack): void {
    const options: EntityViewOptions[] = [
      {
        title: 'billMasterGwApp.pkbillmsCashBack.id',
        value: entity.id,
      },
      {
        title: 'billMasterGwApp.pkbillmsCashBack.bankCommission',
        value: entity.bankCommission,
      },
      {
        title: 'billMasterGwApp.pkbillmsCashBack.clientCommission',
        value: entity.clientCommission,
      },
      {
        title: 'billMasterGwApp.pkbillmsCashBack.name',
        value: entity.name,
      },
      {
        title: 'billMasterGwApp.pkbillmsCashBack.type',
        value: entity.type,
      },
      {
        title: 'billMasterGwApp.pkbillmsCashBack.status',
        value: entity.status,
      },
    ];

    this.entityService.view(options, entity.id);
  }
  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.CASH_BACK_LIST_MODIFICATION,
      alertTranslation: 'billMasterGwApp.pkbillmsCashBack.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'CASH_BACK',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsCashBack',
    };

    this.entityService.history(options);
  }
}
