import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Currency} from './currency.model';
import {CurrencyService} from './currency.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'currency.template.html',
})
export class CurrencyComponent extends EntityBaseComponent<Currency> implements OnInit, OnDestroy {
  constructor(public readonly currencyService: CurrencyService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.currencyService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.CURRENCY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.currencyService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Currency[]>) => this.onDataReceived(response));
  }
}
