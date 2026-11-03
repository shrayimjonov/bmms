import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from '../../../../core/util/request-util';
import { ReconcileDiscrepancy } from './reconcile-discrepancy.model';
import { ApplicationConfigService } from '../../../../core/config/application-config.service';

type EntityResponseType = HttpResponse<ReconcileDiscrepancy>;
type EntityArrayResponseType = HttpResponse<ReconcileDiscrepancy[]>;

@Injectable({ providedIn: 'root' })
export class ReconcileDiscrepancyService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/reconcile-discrepancies', 'bmms');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(reconcileDiscrepancy: ReconcileDiscrepancy): Observable<EntityResponseType> {
    return this.http.post<ReconcileDiscrepancy>(this.resourceUrl, reconcileDiscrepancy, { observe: 'response' });
  }

  update(reconcileDiscrepancy: ReconcileDiscrepancy): Observable<EntityResponseType> {
    return this.http.put<ReconcileDiscrepancy>(`${this.resourceUrl}/${reconcileDiscrepancy.id}`, reconcileDiscrepancy, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ReconcileDiscrepancy>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ReconcileDiscrepancy[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
