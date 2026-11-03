import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ResultData} from "../../../../shared/model/pkbillms/result-data.model";
import {OverpayResult} from "./overpay-result.model";
import {ApplicationConfigService} from "../../../../core/config/application-config.service";

@Injectable({providedIn: 'root'})
export class OverpayService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/overpay', 'bmms');

  constructor(protected http: HttpClient,
              private applicationConfigService: ApplicationConfigService) {
  }

  cancelPay(transactionId: number): Observable<ResultData<OverpayResult>> {
    return this.http.get<ResultData<OverpayResult>>(`${this.resourceUrl}/cancel_gateway/${transactionId}`, {observe: 'body'});
  }
}
