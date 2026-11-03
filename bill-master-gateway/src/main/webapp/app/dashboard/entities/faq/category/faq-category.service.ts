import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {isPresent} from 'app/core/util/operators';
import {ApplicationConfigService} from 'app/core/config/application-config.service';
import {FaqCategory, getFaqCategoryIdentifier} from './faq-category.model';
import {createRequestOption} from "../../../../core/util/request-util";
import {EntityUpdateOptions} from "../../../../shared/entity/entity-update/entity-update-options.model";
import {FaqCategoryUpdateComponent} from "./faq-category-update.component";
import {EntityService} from "../../../../shared/entity/entity.service";
import {Event} from "../../../../config/event.constants";

export type EntityResponseType = HttpResponse<FaqCategory>;
export type EntityArrayResponseType = HttpResponse<FaqCategory[]>;

@Injectable({providedIn: 'root'})
export class FaqCategoryService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/faq-categories', 'faqms');

  constructor(protected http: HttpClient,
              private entityService: EntityService,
              protected applicationConfigService: ApplicationConfigService) {
  }

  create(faqCategory: FaqCategory): Observable<EntityResponseType> {
    return this.http.post<FaqCategory>(this.resourceUrl, faqCategory, {observe: 'response'});
  }

  update(faqCategory: FaqCategory): Observable<EntityResponseType> {
    return this.http.put<FaqCategory>(`${this.resourceUrl}/${getFaqCategoryIdentifier(faqCategory) as number}`, faqCategory, {
      observe: 'response',
    });
  }

  partialUpdate(faqCategory: FaqCategory): Observable<EntityResponseType> {
    return this.http.patch<FaqCategory>(`${this.resourceUrl}/${getFaqCategoryIdentifier(faqCategory) as number}`, faqCategory, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<FaqCategory>(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<FaqCategory[]>(this.resourceUrl, {params: options, observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  addFaqCategoryToCollectionIfMissing(
    faqCategoryCollection: FaqCategory[],
    ...faqCategoriesToCheck: (FaqCategory | null | undefined)[]
  ): FaqCategory[] {
    const faqCategories: FaqCategory[] = faqCategoriesToCheck.filter(isPresent);
    if (faqCategories.length > 0) {
      const faqCategoryCollectionIdentifiers = faqCategoryCollection.map(faqCategoryItem => getFaqCategoryIdentifier(faqCategoryItem)!);
      const faqCategoriesToAdd = faqCategories.filter(faqCategoryItem => {
        const faqCategoryIdentifier = getFaqCategoryIdentifier(faqCategoryItem);
        if (faqCategoryIdentifier == null || faqCategoryCollectionIdentifiers.includes(faqCategoryIdentifier)) {
          return false;
        }
        faqCategoryCollectionIdentifiers.push(faqCategoryIdentifier);
        return true;
      });
      return [...faqCategoriesToAdd, ...faqCategoryCollection];
    }
    return faqCategoryCollection;
  }

  openUpdate(faqCategory?: FaqCategory, duplicate?: boolean): void {
    const options: EntityUpdateOptions<FaqCategoryUpdateComponent> = {
      component: FaqCategoryUpdateComponent,
      componentInstances: {
        data: faqCategory || {},
        duplicate: duplicate
      },
      modalOptions: {size: 'lg'},
    };

    this.entityService.update(options);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.FAQ_CATEGORY_LIST_MODIFICATION,
      alertTranslation: 'faqMsApp.faqCategory.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }
}
