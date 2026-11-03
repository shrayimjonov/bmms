import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';
import {CategoryProxy} from 'app/dashboard/entities/_temp_payment/model/category-proxy.model';
import {CheckRequest} from 'app/shared/model/pkbillms/check-request.model';
import {SimpleBillResponse} from 'app/shared/model/pkbillms/simple-bill-response.model';
import {PayRequest} from 'app/shared/model/pkbillms/pay-request.model';
import {Recipient} from '../recipient/recipient.model';
import {Field} from '../field/field.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {RECIPIENTS_LOGO_URL} from '../../../app.constants';
import {BmAlertService} from '../../../shared/component/bm-alert/bm-alert.service';
import {LanguageService} from '../../../core/util/language.service';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private resourceUrl = this.appConfigService.getEndpointFor('api/agent', 'bmms');
  private resourceUrlUnauthorized = this.appConfigService.getEndpointFor('api/ui', 'bmms');
  private data$: Observable<CategoryProxy[]>;

  constructor(
    private readonly http: HttpClient,
    private readonly alertService: BmAlertService,
    private readonly langService: LanguageService,
    private readonly appConfigService: ApplicationConfigService
  ) {}

  getServices(force?: boolean): Observable<CategoryProxy[]> {
    if (this.data$ && !force) {
      return this.data$;
    }

    this.data$ = this.http.get('content/json/mini-services.json', { observe: 'body' }).pipe(
      map(data => this.convertServices(data)),
      shareReplay()
    );

    return this.data$;
  }

  private convertServices(data: any): CategoryProxy[] {
    // get converted data from server (in future)
    return data.categories.map(category => {
      const proxy: CategoryProxy = category;
      proxy.recipients = data.recipients.filter((recipient: Recipient) => recipient['categoryId'] === category.id);
      proxy.recipients.forEach((filtered: Recipient) => {
        filtered.fields = data.fields.filter((field: Field) => field['recipientId'] === filtered.id);
      });

      return proxy;
    });
  }

  check(req: CheckRequest, unauthorized?: boolean): Observable<SimpleBillResponse> {
    const urlSuffix = unauthorized ? '/unregistered-check' : '/check/v2';
    return this.http.post(this.getResourceUrl(urlSuffix, unauthorized), req, { observe: 'body' });
  }

  pay(req: PayRequest, unauthorized?: boolean): Observable<SimpleBillResponse> {
    const urlSuffix = unauthorized ? '/unregistered-pay' : '/pay/v2';
    return this.http.post(this.getResourceUrl(urlSuffix, unauthorized), req, { observe: 'body' });
  }

  private getResourceUrl(suffix = '', unauthorized?: boolean): string {
    if (unauthorized) {
      return this.resourceUrlUnauthorized + suffix;
    }

    return this.resourceUrl + suffix;
  }

  getRecipientWithoutAuthorization(id: number | string): Observable<Recipient> {
    if (typeof id === 'string') {
      id = Number(id);
    }
    return this.http.get<Recipient>(`${this.resourceUrlUnauthorized}/recipients/${id}`, { observe: 'body' });
  }

  getRecipientLogoUrl(logo: string): string {
    return `${this.appConfigService.getEndpointFor(RECIPIENTS_LOGO_URL)}/${logo}`;
  }

  showResponseExceptionMessage(response: SimpleBillResponse): void {
    const lang = this.langService.getCurrent();
    const message = response.exception.langs ? response.exception.langs?.[lang] : response.exception[lang]; // TODO: Fix response exception langs from server
    this.alertService.error(message, null, null, 4000);
  }
}
