import {Component, OnDestroy, OnInit} from "@angular/core";
import {FaqCategoryService} from "../faq/category/faq-category.service";
import {FaqService} from "../faq/faq.service";
import {FaqCategory} from "../faq/category/faq-category.model";
import {Faq} from "../faq/faq.model";
import {finalize, pluck, takeUntil} from "rxjs/operators";
import {I18n} from "../../../shared/model/pkbillms/i18n.model";
import {BmI18nService} from "../../../shared/component/bm-i18n-input/bm-i18n.service";
import {Subject} from "rxjs";
import {Editor, toHTML} from "ngx-editor";
import {BmAlertService} from "../../../shared/component/bm-alert/bm-alert.service";

@Component({
  templateUrl: 'help.template.html',
  styleUrls: ['help.style.scss'],
})
export class HelpComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  categories: FaqCategory[];
  selectedFaq: Faq;
  selectedCategory: FaqCategory;
  editor = new Editor();
  html: any;

  constructor(private readonly faqCategoryService: FaqCategoryService,
              private readonly i18nService: BmI18nService,
              private readonly alertService: BmAlertService,
              private readonly faqService: FaqService) {
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCategories(): void {
    this.faqCategoryService.query()
      .pipe(
        takeUntil(this.destroy$),
        pluck('body')
      )
      .subscribe((faqCategories: FaqCategory[]) => {
        this.categories = faqCategories;
        if (!faqCategories?.length) {
          this.alertService.warning('global.data.notFound')
        }
      })
  }

  loadFaqsByCategory(category: FaqCategory): void {
    if (category.faqs) return;
    // this.selectedCategory = category;

    const req = {
      size: 1000,
      'questionCategoryId.equals': category.id
    }
    category.loading = true;
    this.faqService.query(req)
      .pipe(
        takeUntil(this.destroy$),
        pluck('body'),
        finalize(() => category.loading = false)
      )
      .subscribe((faqs: Faq[]) =>
        category.faqs = faqs.filter(faq => faq?.faqCategory?.id === category.id)
      )
  }

  onFaqSelected(faq: Faq): void {
    if (faq.id) {
      this.selectedFaq = faq;
      this.html = toHTML(JSON.parse(this.getI18nText(faq.answers)))
    }
  }

  getI18nText(i18ns: I18n[]): string {
    return this.i18nService.getText(i18ns);
  }
}
