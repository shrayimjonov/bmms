import {HttpClient, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {isPresent} from '../../../core/util/operators';
import {EntityService} from '../../../shared/entity/entity.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {getPcBinIdentifier, HumoBin} from './humo-bin.model';
import {HumoBinUpdateModalComponent} from './humo-bin-update-modal.component';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';

export type EntityResponseType = HttpResponse<HumoBin>;
export type EntityArrayResponseType = HttpResponse<HumoBin[]>;

@Injectable({providedIn: 'root'})
export class HumoBinService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/pc-bins', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    protected applicationConfigService: ApplicationConfigService
  ) {
  }

  create(pcBin: HumoBin): Observable<EntityResponseType> {
    return this.http.post<HumoBin>(this.resourceUrl, pcBin, {observe: 'response'});
  }

  update(pcBin: HumoBin): Observable<EntityResponseType> {
    return this.http.put<HumoBin>(this.resourceUrl, pcBin, {observe: 'response'});
  }

  partialUpdate(pcBin: HumoBin): Observable<EntityResponseType> {
    return this.http.patch<HumoBin>(`${this.resourceUrl}/${getPcBinIdentifier(pcBin) as number}`, pcBin, {observe: 'response'});
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<HumoBin>(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<HumoBin[]>(this.resourceUrl, {params: options, observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  addPcBinToCollectionIfMissing(pcBinCollection: HumoBin[], ...pcBinsToCheck: (HumoBin | null | undefined)[]): HumoBin[] {
    const pcBins: HumoBin[] = pcBinsToCheck.filter(isPresent);
    if (pcBins.length > 0) {
      const pcBinCollectionIdentifiers = pcBinCollection.map(pcBinItem => getPcBinIdentifier(pcBinItem)!);
      const pcBinsToAdd = pcBins.filter(pcBinItem => {
        const pcBinIdentifier = getPcBinIdentifier(pcBinItem);
        if (pcBinIdentifier == null || pcBinCollectionIdentifiers.includes(pcBinIdentifier)) {
          return false;
        }
        pcBinCollectionIdentifiers.push(pcBinIdentifier);
        return true;
      });
      return [...pcBinsToAdd, ...pcBinCollection];
    }
    return pcBinCollection;
  }

  getFilterFields(): NglFilterField[] {
    return [
      {name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id'},
      {name: 'bin.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsPcBin.bin'},
      {name: 'bankName.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsPcBin.bankName'},
      {
        name: 'useCredit.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsPcBin.useCredit'
      },
      {name: 'comment.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsPcBin.comment'},
      {name: 'useDebit.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsPcBin.useDebit'},
      {
        name: 'useReverse.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsPcBin.useReverse'
      },
      {
        name: 'useReconcile.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsPcBin.useReconcile'
      }
    ]
  }

  openUpdate(humoBin?: HumoBin, duplicate?: boolean): void {
    const options: EntityUpdateOptions<HumoBinUpdateModalComponent> = {
      component: HumoBinUpdateModalComponent,
      componentInstances: {
        data: humoBin || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(humoBin: HumoBin): void {
    const options: EntityViewOptions[] = [
      {title: 'global.field.id', value: humoBin.id},
      {title: 'billMasterGwApp.pkbillmsPcBin.bin', value: humoBin.bin},
      {title: 'billMasterGwApp.pkbillmsPcBin.bankName', value: humoBin.bankName},
      {title: 'billMasterGwApp.pkbillmsPcBin.pcType', value: humoBin.pcType},
      {title: 'billMasterGwApp.pkbillmsPcBin.useCredit', value: humoBin.useCredit},
      {title: 'billMasterGwApp.pkbillmsPcBin.useDebit', value: humoBin.useDebit},
      {title: 'billMasterGwApp.pkbillmsPcBin.useReverse', value: humoBin.useReverse},
      {title: 'billMasterGwApp.pkbillmsPcBin.useReconcile', value: humoBin.useReconcile},
      {title: 'billMasterGwApp.pkbillmsPcBin.comment', value: humoBin.comment},
    ];

    this.entityService.view(options, humoBin.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.HUMO_BIN_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsPcBin.delete',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'HumoBin',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsPcBin',
    };

    this.entityService.history(options);
  }
}
