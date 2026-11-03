import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

import {FaqCategory} from './faq-category.model';
import {FaqCategoryService} from './faq-category.service';
import {EntityBaseComponent} from "../../../../shared/entity/entity-base-component";
import {Event} from "../../../../config/event.constants";
import {BmI18nService} from "../../../../shared/component/bm-i18n-input/bm-i18n.service";
import {I18n} from "../../../../shared/model/pkbillms/i18n.model";

@Component({
  templateUrl: 'faq-category.template.html',
})
export class FaqCategoryComponent extends EntityBaseComponent<FaqCategory> implements OnInit, OnDestroy {
  constructor(
    public readonly faqCategoryService: FaqCategoryService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    private readonly i18nService: BmI18nService,
    protected modalService: NgbModal
  ) {
    super()
  }

  ngOnInit(): void {
    this.loadPage();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.FAQ_CATEGORY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.faqCategoryService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<FaqCategory[]>) => this.onDataReceived(response));
  }

  getI18nText(i18ns: I18n[]): string {
    return this.i18nService.getText(i18ns);
  }

}
