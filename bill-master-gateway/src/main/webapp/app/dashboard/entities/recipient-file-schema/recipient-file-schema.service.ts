import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {isPresent} from 'app/core/util/operators';
import {ApplicationConfigService} from 'app/core/config/application-config.service';
import {createRequestOption} from 'app/core/util/request-util';
import {getRecipientFileSchemaIdentifier, RecipientFileSchema} from './recipient-file-schema.model';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {RecipientFileSchemaUpdateComponent} from './recipient-file-schema-update.component';

export type EntityResponseType = HttpResponse<RecipientFileSchema>;
export type EntityArrayResponseType = HttpResponse<RecipientFileSchema[]>;

@Injectable({ providedIn: 'root' })
export class RecipientFileSchemaService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/recipient-file-schemas', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    protected applicationConfigService: ApplicationConfigService
  ) {}

  create(recipientFileSchema: RecipientFileSchema): Observable<EntityResponseType> {
    return this.http.post<RecipientFileSchema>(this.resourceUrl, recipientFileSchema, { observe: 'response' });
  }

  update(recipientFileSchema: RecipientFileSchema): Observable<EntityResponseType> {
    return this.http.put<RecipientFileSchema>(
      `${this.resourceUrl}/${getRecipientFileSchemaIdentifier(recipientFileSchema) as number}`,
      recipientFileSchema,
      { observe: 'response' }
    );
  }

  partialUpdate(recipientFileSchema: RecipientFileSchema): Observable<EntityResponseType> {
    return this.http.patch<RecipientFileSchema>(
      `${this.resourceUrl}/${getRecipientFileSchemaIdentifier(recipientFileSchema) as number}`,
      recipientFileSchema,
      { observe: 'response' }
    );
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<RecipientFileSchema>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RecipientFileSchema[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/cascaded/${id}`, { observe: 'response' });
  }

  addRecipientFileSchemaToCollectionIfMissing(
    recipientFileSchemaCollection: RecipientFileSchema[],
    ...recipientFileSchemasToCheck: (RecipientFileSchema | null | undefined)[]
  ): RecipientFileSchema[] {
    const recipientFileSchemas: RecipientFileSchema[] = recipientFileSchemasToCheck.filter(isPresent);
    if (recipientFileSchemas.length > 0) {
      const recipientFileSchemaCollectionIdentifiers = recipientFileSchemaCollection.map(
        recipientFileSchemaItem => getRecipientFileSchemaIdentifier(recipientFileSchemaItem)!
      );
      const recipientFileSchemasToAdd = recipientFileSchemas.filter(recipientFileSchemaItem => {
        const recipientFileSchemaIdentifier = getRecipientFileSchemaIdentifier(recipientFileSchemaItem);
        if (recipientFileSchemaIdentifier == null || recipientFileSchemaCollectionIdentifiers.includes(recipientFileSchemaIdentifier)) {
          return false;
        }
        recipientFileSchemaCollectionIdentifiers.push(recipientFileSchemaIdentifier);
        return true;
      });
      return [...recipientFileSchemasToAdd, ...recipientFileSchemaCollection];
    }
    return recipientFileSchemaCollection;
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'name.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsRecipientFileSchema.name',
      },
      {
        name: 'idList.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsRecipientFileSchema.idList',
      },
      {
        name: 'root.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.bmmsRecipientFileSchema.root',
      },
    ];
  }

  openUpdate(recipientFileSchema?: RecipientFileSchema, duplicate?: boolean): void {
    const options: EntityUpdateOptions<RecipientFileSchemaUpdateComponent> = {
      component: RecipientFileSchemaUpdateComponent,
      componentInstances: {
        data: recipientFileSchema || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(recipientFileSchema: RecipientFileSchema): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: recipientFileSchema.id },
      { title: 'billMasterGwApp.pkbillmsRecipientFileSchema.name', value: recipientFileSchema.name },
      { title: 'billMasterGwApp.pkbillmsRecipientFileSchema.idList', value: recipientFileSchema.idList },
      { title: 'billMasterGwApp.pkbillmsRecipientFileSchema.root', value: recipientFileSchema.root },
    ];

    this.entityService.view(options, recipientFileSchema.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.RECIPIENT_FILE_SCHEMA_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsRecipientFileSchema.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'RecipientFileSchema',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsRecipientFileSchema',
    };

    this.entityService.history(options);
  }
}
