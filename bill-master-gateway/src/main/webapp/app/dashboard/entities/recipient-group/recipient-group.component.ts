import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {RecipientGroup} from './recipient-group.model';
import {RecipientGroupService} from './recipient-group.service';
import {Event} from '../../../config/event.constants';
import {RecipientService} from '../recipient/recipient.service';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'recipient-group.template.html',
})
export class RecipientGroupComponent extends EntityBaseComponent<RecipientGroup> implements OnInit, OnDestroy {
  readonly exclude = ['recipients'];

  constructor(
    public readonly recipientGroupService: RecipientGroupService,
    public readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.recipientGroupService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.RECIPIENT_GROUP_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.recipientGroupService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<RecipientGroup[]>) => this.onDataReceived(response));
  }
}
