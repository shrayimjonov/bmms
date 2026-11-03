import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {Report} from './report.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';

@Injectable({ providedIn: 'root' })
export class ReportService {
  public resourceUrl = this.appConfigService.getEndpointFor('api/reports', 'reportms');

  constructor(private readonly http: HttpClient,
              private readonly appConfigService: ApplicationConfigService) {}

  create(report: Report): Observable<Report> {
    return this.http.post<Report>(this.resourceUrl, report, { observe: 'body' });
  }

  update(report: Report): Observable<Report> {
    return this.http.put<Report>(`${this.resourceUrl}/${report.id}`, report, { observe: 'body' });
  }

  find(id: number): Observable<Report> {
    return this.http.get<Report>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<Report[]>> {
    return this.http.get<Report[]>(this.resourceUrl, { params: createRequestOption(req), observe: 'response' });
  }

  delete(id: number): Observable<{}> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }
}
