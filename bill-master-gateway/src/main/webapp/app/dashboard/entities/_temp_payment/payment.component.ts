import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {PaymentService} from './payment.service';
import {Subject} from 'rxjs';
import {CategoryProxy} from './model/category-proxy.model';
import {RecipientProxy} from './model/recipient-proxy.model';
import {PaymentModalComponent} from './payment-modal.component';
import {finalize, takeUntil} from 'rxjs/operators';
import {LanguageService} from '../../../core/util/language.service';
import {EntityService} from '../../../shared/entity/entity.service';

@Component({
  templateUrl: 'payment.component.html',
  styleUrls: ['payment.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  categories: CategoryProxy[];
  loading: boolean;
  searchText: string;

  lang = this.langService.getCurrent();

  constructor(public readonly paymentService: PaymentService,
              private readonly langService: LanguageService,
              private readonly entityService: EntityService,
              private readonly cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.loadServices();
    this.registerOnChangeLang();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadServices(): void {
    this.loading = true;
    this.paymentService
      .getServices()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe((categories: CategoryProxy[]) => (this.categories = categories));
  }

  private registerOnChangeLang(): void {
    this.langService
      .onChange()
      .pipe(takeUntil(this.destroy$))
      .subscribe(lang => (this.lang = lang));
  }

  openRecipient(recipientProxy: RecipientProxy, categoryName: string): void {
    if (!recipientProxy.fields?.length) return;

    this.entityService.update<PaymentModalComponent>({
      component: PaymentModalComponent,
      componentInstances: {
        recipient: recipientProxy,
        categoryName: categoryName,
        lang: this.lang,
      },
      modalOptions: {size: 'md'},
    });
  }
}
