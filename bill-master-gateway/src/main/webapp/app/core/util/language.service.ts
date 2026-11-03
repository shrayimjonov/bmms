import {Injectable} from '@angular/core';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {mergeMap} from 'rxjs/operators';
import {Observable, of} from 'rxjs';
import {SessionStorageService} from 'ngx-webstorage';
import {LANGUAGES} from "../../config/language.constants";

@Injectable({providedIn: 'root'})
export class LanguageService {
  private currentLang: string;

  constructor(private readonly translateService: TranslateService,
              private readonly sessionStorage: SessionStorageService) {
    this.currentLang = this.translateService.currentLang;
    this.onChange().subscribe(lang => (this.currentLang = lang));
  }

  getCurrent(): string {
    return this.currentLang;
  }

  change(langKey: string): Observable<any> {
    return this.translateService.use(langKey);
  }

  onChange(): Observable<string> {
    return this.translateService.onLangChange.pipe(
      mergeMap((event: LangChangeEvent) => {
        this.sessionStorage.store('locale', event.lang);
        return of(event.lang);
      })
    );
  }

  getTranslatedLangKeys(): { label: string, value: string }[] {
    return LANGUAGES.map(langKey => ({
      label: this.translateService.instant('global.langKey.' + langKey),
      value: langKey
    }))
  }
}
