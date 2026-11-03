import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Category} from './category.model';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {CategoryUpdateModalComponent} from './category-update-modal.component';

@Injectable({ providedIn: 'root' })
export class CategoryService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/categories', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(category: Category): Observable<Category> {
    return this.http.post<Category>(this.resourceUrl, category, { observe: 'body' });
  }

  update(category: Category): Observable<Category> {
    return this.http.put<Category>(`${this.resourceUrl}/${category.id}`, category, { observe: 'body' });
  }

  find(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<Category[]>> {
    const options = createRequestOption(req);
    return this.http.get<Category[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'shortName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsCategory.shortName',
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsCategory.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(category?: Category, duplicate?: boolean): void {
    const options: EntityUpdateOptions<CategoryUpdateModalComponent> = {
      component: CategoryUpdateModalComponent,
      componentInstances: {
        duplicate: duplicate,
        data: category || {}
      },
    };

    this.entityService.update(options);
  }

  openView(category: Category): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: category.id },
      { title: 'billMasterGwApp.pkbillmsCategory.logo', value: category.logo },
      { title: 'billMasterGwApp.pkbillmsCategory.shortName', value: category.shortName },
      { title: 'billMasterGwApp.pkbillmsCategory.active', value: category.active, type: 'boolean', boolean: 'active' },
    ];

    this.entityService.view(options, category.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.CATEGORY_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsCategory.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Category',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsCategory',
    };

    this.entityService.history(options);
  }
}
