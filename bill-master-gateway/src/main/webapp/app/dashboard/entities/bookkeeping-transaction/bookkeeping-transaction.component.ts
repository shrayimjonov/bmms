import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {BookkeepingTransaction} from '../../../shared/model/pkbillms/bookkeeping-transaction.model';
import {BookkeepingTransactionService} from './bookkeeping-transaction.service';
import {BookkeepingTransactionType} from '../../../shared/model/enumerations/bookkeeping-transaction-type.model';
import {TranslateService} from '@ngx-translate/core';
import {Title} from '@angular/platform-browser';
import {ActivatedRoute} from '@angular/router';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';
import {Menu} from '../../dashboard-sidebar-menus';

@Component({
  templateUrl: 'bookkeeping-transaction.template.html',
  styleUrls: ['bookkeeping-transaction-status.style.scss'],
})
export class BookkeepingTransactionComponent extends EntityBaseComponent<BookkeepingTransaction> implements OnInit, OnDestroy {
  readonly tabs: Menu[] = [
    {
      title: 'internal.example.com',
      icon: 'arrow-down',
      value: internal.example.com,
    },
    {
      title: 'billMasterGwApp.BookkeepingTransactionType.EXTERNAL',
      icon: 'arrow-up',
      value: BookkeepingTransactionType.EXTERNAL,
    },
  ];

  private type = internal.example.com;

  constructor(public readonly bookkeepingTransactionService: BookkeepingTransactionService,
              private readonly translateService: TranslateService,
              private readonly titleService: Title,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.bookkeepingTransactionService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.updateTitle();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onChangeTab(type: BookkeepingTransactionType): void {
    if (this.type === type) return;

    this.type = type;
    this.updateTitle();
    this.loadPage();
  }

  updateTitle(): void {
    const title = this.translateService.instant('billMasterGwApp.pkbillmsBookkeepingTransaction.transactionType.' + this.type);
    this.titleService.setTitle(title);
  }

  loadPage(page?: number): void {
    const req = {...this.getRequestParams(page), 'bookkeepingType.equals': this.type};

    this.loading = true;
    this.bookkeepingTransactionService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<BookkeepingTransaction[]>) => this.onDataReceived(response));
  }
}
