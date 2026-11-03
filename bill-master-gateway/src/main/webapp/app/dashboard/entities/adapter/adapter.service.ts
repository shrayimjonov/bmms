import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Result} from "../../../shared/model/pkbillms/result.model";
import {Adapter} from "./adapter.model";
import {AdapterHistory} from "./history/adapter-history.model";
import {AdapterFileUpload} from "./file-upload/adapter-file-upload.model";
import {ApplicationConfigService} from "../../../core/config/application-config.service";

@Injectable()
export class AdapterService {
  private resourceUrl = this.applicationConfigService.getEndpointFor('api/adapter', 'bmms');
  private historyUrl = this.applicationConfigService.getEndpointFor('api/data-adapter-histories', 'bmms');

  constructor(protected http: HttpClient,
              private applicationConfigService: ApplicationConfigService) {
  }

  sync(adapter: string): Observable<Result> {
    return this.http.get<Result>(`${this.resourceUrl}/sync/${adapter}`, {observe: 'body'});
  }

  syncWithFile(file: AdapterFileUpload): Observable<Result> {
    return this.http.post<Result>(`${this.resourceUrl + '/sync'}/file`, file, {observe: 'body'});
  }

  getListOfAdaptersV3(): Observable<Adapter[]> {
    return this.http.get<Adapter[]>(this.resourceUrl + '/list', {observe: 'body'});
  }

  getHistories(): Observable<HttpResponse<AdapterHistory[]>> {
    return this.http.get<AdapterHistory[]>(this.historyUrl, {observe: 'response'});
  }

  findHistoryByName(name: string): Observable<AdapterHistory> {
    return this.http.get<AdapterHistory>(`${this.historyUrl + '/byName'}/${name}`, {observe: 'body'});
  }
}
