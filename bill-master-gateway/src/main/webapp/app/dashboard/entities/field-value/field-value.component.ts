import {Component, OnDestroy, OnInit} from '@angular/core';
import {FieldValue} from './field-value.model';
import {ActivatedRoute} from '@angular/router';
import {FieldValueService} from './field-value.service';
import {HttpResponse} from '@angular/common/http';
import {FieldService} from '../field/field.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'field-value.template.html',
})
export class FieldValueComponent extends EntityBaseComponent<FieldValue> implements OnInit, OnDestroy {
  readonly exclude = ['titles', 'descriptions'];

  constructor(
    public readonly fieldValueService: FieldValueService,
    public readonly fieldService: FieldService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.fieldValueService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.FIELD_VALUE_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.fieldValueService
      .query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<FieldValue[]>) => this.onDataReceived(response));
  }
}
