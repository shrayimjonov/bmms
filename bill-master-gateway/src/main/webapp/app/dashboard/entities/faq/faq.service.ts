import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {ApplicationConfigService} from 'app/core/config/application-config.service';
import {createRequestOption} from "../../../core/util/request-util";
import {EntityUpdateOptions} from "../../../shared/entity/entity-update/entity-update-options.model";
import {FaqUpdateModalComponent} from "./faq-update-modal.component";
import {EntityService} from "../../../shared/entity/entity.service";
import {Faq} from "./faq.model";
import {Event} from "../../../config/event.constants";

export type EntityResponseType = HttpResponse<Faq>;
export type EntityArrayResponseType = HttpResponse<Faq[]>;

@Injectable({providedIn: 'root'})
export class FaqService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/faqs', 'faqms');

  constructor(protected http: HttpClient,
              protected applicationConfigService: ApplicationConfigService,
              private entityService: EntityService,) {
  }

  create(faq: Faq): Observable<EntityResponseType> {
    return this.http.post<Faq>(this.resourceUrl, faq, {observe: 'response'});
  }

  update(faq: Faq): Observable<EntityResponseType> {
    return this.http.put<Faq>(`${this.resourceUrl}/${faq.id}`, faq, {observe: 'response'});
  }

  partialUpdate(faq: Faq): Observable<EntityResponseType> {
    return this.http.patch<Faq>(`${this.resourceUrl}/${faq.id}`, faq, {observe: 'response'});
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<Faq>(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  getAllByCategoryId(categoryId?: number): Observable<EntityArrayResponseType> {
    return this.http.get<Faq[]>(`${this.resourceUrl}/categoryId/${categoryId}`, {observe: 'response'})
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<Faq[]>(this.resourceUrl, {params: options, observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  openUpdate(faq?: Faq, duplicate?: boolean): void {
    const options: EntityUpdateOptions<FaqUpdateModalComponent> = {
      component: FaqUpdateModalComponent,
      componentInstances: {
        data: faq || {},
        duplicate: duplicate
      },
      modalOptions: {size: 'lg'},
    };

    this.entityService.update(options);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.FAQ_LIST_MODIFICATION,
      alertTranslation: 'faqMsApp.faq.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }
}
