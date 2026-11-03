import {Injector, LOCALE_ID, NgModule} from '@angular/core';
import {registerLocaleData} from '@angular/common';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import locale from '@angular/common/locales/ru';
import {BrowserModule, Title} from '@angular/platform-browser';
import {ServiceWorkerModule} from '@angular/service-worker';
import {MissingTranslationHandler, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {NgxWebstorageModule, SessionStorageService} from 'ngx-webstorage';
import * as dayjs from 'dayjs';
import {NgbDateAdapter, NgbDatepickerConfig} from '@ng-bootstrap/ng-bootstrap';

import {SERVER_API_URL} from './app.constants';
import {ApplicationConfigService} from 'app/core/config/application-config.service';
import './config/dayjs';
import {SharedModule} from 'app/shared/shared.module';
// jhipster-needle-angular-add-module-import JHipster will add new module here
import {NgbDateDayjsAdapter} from './config/datepicker-adapter';
import {httpInterceptorProviders} from './core/interceptor';
import {missingTranslationHandler, translatePartialLoader} from './config/translation.config';
import {APP_ROUTES} from './app.route';
import {RouterModule} from '@angular/router';
import {AppComponent} from './app.component';
import {NZ_I18N, ru_RU} from 'ng-zorro-antd/i18n';
import {NZ_ICONS} from 'ng-zorro-antd/icon';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ErrorComponent} from './core/error/error.component';
import {EntityDeleteModule} from './shared/entity/entity-delete/entity-delete.module';
import {EntityViewModule} from './shared/entity/entity-view/entity-view.module';
import {NgProgressModule} from 'ngx-progressbar';
import {NgProgressHttpModule} from 'ngx-progressbar/http';
import {NZ_ICONS_DEFINITION} from './config/nz-icons';

export let InjectorInstance: Injector;

@NgModule({
  imports: [
    BrowserModule,
    EntityDeleteModule,
    EntityViewModule,
    SharedModule,
    RouterModule.forRoot(APP_ROUTES),
    BrowserAnimationsModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    // Set this to true to enable service worker (PWA)
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: false }),
    HttpClientModule,
    NgxWebstorageModule.forRoot({ prefix: 'jhi', separator: '-', caseSensitive: true }),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: translatePartialLoader,
        deps: [HttpClient],
      },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useFactory: missingTranslationHandler,
      },
    }),
    NgProgressModule.withConfig({
      spinnerPosition: 'left',
      color: '#001529',
    }),
    NgProgressHttpModule,
  ],
  providers: [
    {
      provide: NZ_I18N,
      useValue: ru_RU,
    },
    {
      provide: NZ_ICONS,
      useValue: NZ_ICONS_DEFINITION,
    },
    Title,
    { provide: LOCALE_ID, useValue: 'ru' },
    { provide: NgbDateAdapter, useClass: NgbDateDayjsAdapter },
    httpInterceptorProviders,
  ],
  declarations: [AppComponent, ErrorComponent],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(
    applicationConfigService: ApplicationConfigService,
    dpConfig: NgbDatepickerConfig,
    translateService: TranslateService,
    sessionStorageService: SessionStorageService,
    injector: Injector
  ) {
    applicationConfigService.setEndpointPrefix(SERVER_API_URL);
    registerLocaleData(locale);
    dpConfig.minDate = { year: dayjs().subtract(100, 'year').year(), month: 1, day: 1 };
    translateService.setDefaultLang('ru');
    // if user have changed language and navigates away from the application and back to the application then use previously choosed language
    const langKey = sessionStorageService.retrieve('locale') ?? 'ru';
    translateService.use(langKey);
    InjectorInstance = injector;
  }
}
