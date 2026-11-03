import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Field} from './field.model';
import {FieldService} from './field.service';
import {HttpResponse} from '@angular/common/http';
import {RecipientService} from '../recipient/recipient.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'field.template.html',
})
export class FieldComponent extends EntityBaseComponent<Field> implements OnInit, OnDestroy {
  readonly exclude = ['content', 'descriptions', 'extId', 'filterBy', 'titles', 'recipient'];

  constructor(
    public readonly fieldService: FieldService,
    public readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.fieldService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.FIELD_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.fieldService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Field[]>) => this.onDataReceived(response));
  }
}
