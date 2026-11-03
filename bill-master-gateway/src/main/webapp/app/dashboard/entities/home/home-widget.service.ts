import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/internal/Observable';
import {HttpClient} from '@angular/common/http';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {HomeWidget, UserHomeWidget} from './home-widget.model';
import {shareReplay} from 'rxjs/operators';

@Injectable()
export class HomeWidgetService {
  private readonly resourceUrlForAdmin = this.appConfigService.getEndpointFor('api/home-widgets', 'reportms');
  private readonly resourceUrlForUser = this.appConfigService.getEndpointFor('api/user-cabinet-widgets', 'reportms');

  private widgets$: Observable<HomeWidget[]>;
  constructor(private readonly http: HttpClient, private readonly appConfigService: ApplicationConfigService) {}

  filter(from: any, force?: boolean): Observable<HomeWidget[]> {
    if (this.widgets$ && !force) {
      return this.widgets$;
    }

    this.widgets$ = this.http
      .get<HomeWidget[]>(`${this.resourceUrlForAdmin}/${from}`, { observe: 'body'})
      .pipe(shareReplay());

    return this.widgets$;
  }

  getWidget(from: any): Observable<UserHomeWidget[]> {
    return this.http.get<UserHomeWidget[]>(`${this.resourceUrlForUser}/${from}`, {observe: 'body'})
  }
}
