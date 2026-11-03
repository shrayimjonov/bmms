import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {BookkeepingTemplate} from './bookkeeping-template.model';
import {BookkeepingTemplateService} from './bookkeeping-template.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'bookkeeping-template.template.html',
})
export class BookkeepingTemplateComponent extends EntityBaseComponent<BookkeepingTemplate> implements OnInit, OnDestroy {
  constructor(public readonly bookkeepingTemplateService: BookkeepingTemplateService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.bookkeepingTemplateService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.BOOKKEEPING_TEMPLATE_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.bookkeepingTemplateService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<BookkeepingTemplate[]>) => this.onDataReceived(response));
  }
}
