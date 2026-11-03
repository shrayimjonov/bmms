import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {ApplicationConfigService} from '../../../../core/config/application-config.service';
import {createRequestOption} from '../../../../core/util/request-util';
import {shareReplay, switchMap} from 'rxjs/operators';
import {CommonResult} from '../../../../shared/model/common-result.model';
import {of} from 'rxjs';
import {I18n} from '../../../../shared/model/pkbillms/i18n.model';

@Injectable({providedIn: 'root'})
export class OtherSettingsService {
  private readonly resourceUrl = this.appConfigService.getEndpointFor('api/jobs', 'bmms');
  private actions$: Observable<any[]>;

  constructor(private http: HttpClient, private appConfigService: ApplicationConfigService) {}

  executeAction(req?: any, action?: any): Observable<CommonResult<I18n[]>> {
    const url = `${this.appConfigService.getEndpointFor(`${action.uri}`, 'bmms')}`;

    return this.http.request<CommonResult<I18n[]>>(action.method, url, {
      params: createRequestOption(req),
      observe: 'body',
    });
  }

  getActions(): Observable<any[]> {
    if (this.actions$) {
      return this.actions$;
    }

    this.actions$ = this.http.get<CommonResult<any[]>>(this.resourceUrl, { observe: 'body' }).pipe(
      switchMap((response: CommonResult<any[]>) => of(response.data)),
      shareReplay()
    );

    return this.actions$;
  }
}
