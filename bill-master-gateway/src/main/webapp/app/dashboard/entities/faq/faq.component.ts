import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Faq} from './faq.model';
import {FaqService} from './faq.service';
import {EntityBaseComponent} from "../../../shared/entity/entity-base-component";
import {Event} from "../../../config/event.constants";
import {BmI18nService} from "../../../shared/component/bm-i18n-input/bm-i18n.service";
import {I18n} from "../../../shared/model/pkbillms/i18n.model";

@Component({
  templateUrl: 'faq.template.html',
})
export class FaqComponent extends EntityBaseComponent<Faq> implements OnInit, OnDestroy {

  constructor(public readonly faqService: FaqService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly i18nService: BmI18nService) {
    super()
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute, () => this.loadPage());
    this.registerToModify(Event.FAQ_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    const req = this.getRequestParams(page);
    req.size = 500;
    this.faqService.query()
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Faq[]>) => this.onDataReceived(response));
  }

  getI18nText(i18ns: I18n[]): string {
    return this.i18nService.getText(i18ns);
  }

}
