import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../core/util/request-util';
import { ApplicationConfigService } from '../../core/config/application-config.service';
import { Util } from '../util';

export abstract class EntityBaseService<ENTITY> {
  private readonly _http = Util.inject(HttpClient);
  private readonly _resourceUrl: string;

  protected constructor(private readonly api: string, private readonly microservice?: string) {
    this._resourceUrl = Util.inject(ApplicationConfigService).getEndpointFor(api, microservice);
  }

  create(entity: ENTITY): Observable<ENTITY> {
    return this._http.post<ENTITY>(this._resourceUrl, entity, { observe: 'body' });
  }

  update(entity: ENTITY): Observable<ENTITY> {
    return this._http.put<ENTITY>(`${this._resourceUrl}/${entity['id']}`, entity, { observe: 'body' });
  }

  find(id: number): Observable<ENTITY> {
    return this._http.get<ENTITY>(`${this._resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<ENTITY[]>> {
    const options = createRequestOption(req);
    return this._http.get<ENTITY[]>(this._resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this._http.delete(`${this._resourceUrl}/${id}`, { observe: 'response' });
  }

  get resourceUrl(): string {
    return this._resourceUrl;
  }
}
