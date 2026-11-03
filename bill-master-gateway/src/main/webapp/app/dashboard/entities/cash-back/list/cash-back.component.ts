import { Component, OnInit } from '@angular/core';
import { EntityBaseComponent } from '../../../../shared/entity/entity-base-component';
import { ICashBack } from '../cash-back.model';
import { CashBackService } from '../service/cash-back.service';
import { HttpResponse } from '@angular/common/http';
import { Event } from '../../../../config/event.constants';
import { ActivatedRoute } from '@angular/router';

@Component({
  templateUrl: 'cash-back.component.html',
})
export class CashBackComponent extends EntityBaseComponent<ICashBack> implements OnInit {
  constructor(public cashBackService: CashBackService, private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit() {
    this.filterFields = this.cashBackService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.CASH_BACK_LIST_MODIFICATION, () => this.loadPage());
  }

  loadPage(page?: number | undefined): void {
    this.loading = true;
    this.cashBackService
      .query(this.getRequestParams(page))
      .pipe(this.loadPageOperators())
      .subscribe((response: HttpResponse<ICashBack[]>) => this.onDataReceived(response));
  }
}
