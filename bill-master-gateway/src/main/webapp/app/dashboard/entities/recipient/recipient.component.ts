import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {RecipientService} from './recipient.service';
import {HttpResponse} from '@angular/common/http';
import {Recipient} from './recipient.model';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'recipient.template.html',
  styleUrls: ['recipient-ccy.style.scss'],
})
export class RecipientComponent extends EntityBaseComponent<Recipient> implements OnInit, OnDestroy {
  readonly exclude = ['fullNames', 'lastModifiedBy', 'lastModifiedDate', 'logo', 'titles'];

  constructor(
    public readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.recipientService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.RECIPIENT_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.recipientService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Recipient[]>) => this.onDataReceived(response));
  }
}
