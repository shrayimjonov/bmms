import {Component, OnDestroy, OnInit} from '@angular/core';
import {Prefix} from './prefix.model';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {PrefixService} from './prefix.service';
import {FieldService} from '../field/field.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'prefix.template.html',
})
export class PrefixComponent extends EntityBaseComponent<Prefix> implements OnInit, OnDestroy {
  readonly exclude = ['field', 'output'];

  constructor(
    public readonly prefixService: PrefixService,
    public readonly fieldService: FieldService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.prefixService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.PREFIX_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.prefixService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Prefix[]>) => this.onDataReceived(response));
  }
}
