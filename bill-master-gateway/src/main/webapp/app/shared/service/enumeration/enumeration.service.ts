import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Enumeration} from "./enumeration.model";
import {ApplicationConfigService} from "../../../core/config/application-config.service";
import {Observable, of} from "rxjs";
import {catchError, flatMap, shareReplay} from "rxjs/operators";
import {BmI18nService} from "../../component/bm-i18n-input/bm-i18n.service";

@Injectable({providedIn: 'root'})
export class EnumerationService {
  private readonly resourceUrl = this.appConfigService.getEndpointFor('api/enum-state', 'bmms');
  private readonly cache$ = new Map<string, Observable<string[]>>();

  constructor(private readonly http: HttpClient,
              private readonly i18nService: BmI18nService,
              private readonly appConfigService: ApplicationConfigService) {
  }

  getOnlyValues(enumClassName: string, enumClass: {}, force?: boolean): Observable<string[]> {
    const cachedData = this.cache$.get(enumClassName);

    if (cachedData && !force) {
      return cachedData;
    }

    const newData = this.http.get<Enumeration[]>(this.resourceUrl, {params: {enum: enumClassName}, observe: 'body'})
      .pipe(
        flatMap((enums: Enumeration[]) => {
          if (!enums?.length) {
            return of(Object.keys(enumClass));
          }

          return of(enums.map(e => e.i18ns ? this.i18nService.getText(e.i18ns) : e.name));
        }),
        catchError(() => of(Object.keys(enumClass))),
        shareReplay()
      );

    this.cache$.set(enumClassName, newData);

    return newData;
  }
}
