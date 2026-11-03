import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {RecipientFileSchema} from './recipient-file-schema.model';
import {HttpResponse} from '@angular/common/http';
import {RecipientFileSchemaService} from './recipient-file-schema.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'recipient-file-schema.template.html',
})
export class RecipientFileSchemaComponent extends EntityBaseComponent<RecipientFileSchema> implements OnInit, OnDestroy {
  constructor(
    public readonly recipientFileSchemaService: RecipientFileSchemaService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super()
  }

  ngOnInit(): void {
    this.filterFields = this.recipientFileSchemaService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.RECIPIENT_FILE_SCHEMA_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.recipientFileSchemaService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<RecipientFileSchema[]>) => this.onDataReceived(response));
  }
}
