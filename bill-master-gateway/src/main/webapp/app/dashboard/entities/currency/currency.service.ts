import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

import {isPresent} from 'app/core/util/operators';
import {ApplicationConfigService} from 'app/core/config/application-config.service';
import {createRequestOption} from 'app/core/util/request-util';
import {Currency, getCurrencyIdentifier} from './currency.model';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {EntityService} from '../../../shared/entity/entity.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {CurrencyUpdateComponent} from './currency-update.component';
import * as dayjs from 'dayjs';

export type EntityResponseType = HttpResponse<Currency>;
export type EntityArrayResponseType = HttpResponse<Currency[]>;

@Injectable({ providedIn: 'root' })
export class CurrencyService implements IEntityConfig {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/currencies', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    protected applicationConfigService: ApplicationConfigService
  ) {}

  create(currency: Currency): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(currency);
    return this.http
      .post<Currency>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(currency: Currency): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(currency);
    return this.http
      .put<Currency>(`${this.resourceUrl}/${getCurrencyIdentifier(currency) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(currency: Currency): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(currency);
    return this.http
      .patch<Currency>(`${this.resourceUrl}/${getCurrencyIdentifier(currency) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<Currency>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<Currency[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addCurrencyToCollectionIfMissing(currencyCollection: Currency[], ...currenciesToCheck: (Currency | null | undefined)[]): Currency[] {
    const currencies: Currency[] = currenciesToCheck.filter(isPresent);
    if (currencies.length > 0) {
      const currencyCollectionIdentifiers = currencyCollection.map(currencyItem => getCurrencyIdentifier(currencyItem)!);
      const currenciesToAdd = currencies.filter(currencyItem => {
        const currencyIdentifier = getCurrencyIdentifier(currencyItem);
        if (currencyIdentifier == null || currencyCollectionIdentifiers.includes(currencyIdentifier)) {
          return false;
        }
        currencyCollectionIdentifiers.push(currencyIdentifier);
        return true;
      });
      return [...currenciesToAdd, ...currencyCollection];
    }
    return currencyCollection;
  }

  protected convertDateFromClient(currency: Currency): Currency {
    return Object.assign({}, currency, {
      date: currency.date ? dayjs(currency.date) : null,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.date = res.body.date ? dayjs(res.body.date) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((currency: Currency) => {
        currency.date = currency.date ? dayjs(currency.date) : undefined;
      });
    }
    return res;
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'code.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsCurrency.code',
      },
      {
        name: 'ccy.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsCurrency.ccy',
      },
      {
        name: 'nameRu.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsCurrency.nameRu',
      },
      {
        name: 'rate.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.bmmsCurrency.rate',
      },
      {
        name: 'sellRate.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.bmmsCurrency.sellRate',
      },
      {
        name: 'buyRate.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.bmmsCurrency.buyRate',
      },
      {
        name: 'ccyGroup.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsCurrency.ccyGroup',
      },
    ];
  }

  openUpdate(currency?: Currency, duplicate?: boolean): void {
    const options: EntityUpdateOptions<CurrencyUpdateComponent> = {
      component: CurrencyUpdateComponent,
      componentInstances: {
        data: currency || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(currency: Currency): void {
    const options: EntityViewOptions[] = [
      { title: 'billMasterGwApp.bmmsCurrency.code', value: currency.code },
      { title: 'billMasterGwApp.bmmsCurrency.ccy', value: currency.ccy },
      { title: 'billMasterGwApp.bmmsCurrency.nameRu', value: currency.nameRu },
      { title: 'billMasterGwApp.bmmsCurrency.rate', value: currency.rate },
      { title: 'billMasterGwApp.bmmsCurrency.sellRate', value: currency.sellRate },
      { title: 'billMasterGwApp.bmmsCurrency.buyRate', value: currency.buyRate },
      {
        title: 'billMasterGwApp.bmmsCurrency.date',
        value: currency.date,
        type: 'date',
      },
      {
        title: 'billMasterGwApp.bmmsCurrency.active',
        value: currency.active,
        type: 'boolean',
        boolean: 'active',
      },
      { title: 'billMasterGwApp.bmmsCurrency.ccyGroup', value: currency.ccyGroup },
    ];

    this.entityService.view(options, currency.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.CURRENCY_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.bmmsCurrency.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Currency',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.bmmsCurrency',
    };

    this.entityService.history(options);
  }
}
