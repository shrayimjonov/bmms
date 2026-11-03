import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ResponseField} from './response-field.model';
import {ActivatedRoute} from '@angular/router';
import {ResponseFieldService} from './response-field.service';
import {HttpResponse} from '@angular/common/http';
import {RecipientService} from '../recipient/recipient.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'response-field.template.html',
})
export class ResponseFieldComponent extends EntityBaseComponent<ResponseField> implements OnInit, OnDestroy {
  readonly exclude = ['recipient', 'labels'];

  constructor(
    public readonly responseFieldService: ResponseFieldService,
    public readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.responseFieldService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.RESPONSE_FIELD_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.responseFieldService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<ResponseField[]>) => this.onDataReceived(response));
  }
}
