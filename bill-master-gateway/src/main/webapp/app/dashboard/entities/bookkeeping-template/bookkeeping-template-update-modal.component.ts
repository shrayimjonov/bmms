import {Component, OnDestroy, OnInit} from '@angular/core';

import {BookkeepingTemplateService} from './bookkeeping-template.service';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {BookkeepingAmountCalculateType, BookkeepingTemplate} from './bookkeeping-template.model';
import {BktPeriod} from '../../../shared/model/enumerations/bkt-period.model';
import {PayType} from '../../../shared/model/enumerations/pay-type.model';
import {BookkeepingTransactionType} from '../../../shared/model/enumerations/bookkeeping-transaction-type.model';
import {GatewayService} from '../gateway/gateway.service';
import {RecipientService} from '../recipient/recipient.service';
import {BookkeepingPayType} from '../../../shared/model/enumerations/bookkeeping-pay-type.model';
import {Event} from '../../../config/event.constants';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'bookkeeping-template-update-modal.template.html',
})
export class BookkeepingTemplateUpdateModalComponent extends EntityUpdateBaseComponent<BookkeepingTemplate, BookkeepingTemplateService> implements OnInit, OnDestroy {
  readonly periods = Object.keys(BktPeriod);
  readonly payTypes = Object.keys(PayType);
  readonly bookkeepingTypes = Object.keys(BookkeepingTransactionType);
  readonly amountCalculateTypes = Object.keys(BookkeepingAmountCalculateType);
  readonly bookkeepingPayTypes = Object.keys(BookkeepingPayType);

  constructor(public readonly gatewayService: GatewayService,
              public readonly recipientService: RecipientService,
              private readonly bookkeepingTemplateService: BookkeepingTemplateService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, bookkeepingTemplateService, Event.BOOKKEEPING_TEMPLATE_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      groupId: [null, Validators.required],
      sMfo: [null, Validators.required],
      sInn: [null, Validators.required],
      sAccount: [null, Validators.required],
      rMfo: [null, Validators.required],
      rInn: [null, Validators.required],
      rAccount: [null, Validators.required],
      calculateAmountRule: [null, Validators.required],
      period: [null, Validators.required],
      payTypes: [null, Validators.required],
      desc: [null, Validators.required],
      sType: [null, Validators.required],
      sTypeIds: [null, Validators.required],
      bookkeepingType: [null, Validators.required],
      name: [null, Validators.required],
      amountCalculateType: [null, Validators.required],
      gateway: [null],
      recipient: [null],
      docType: [null],
      docCode: [null],
      docTaskCode: [null],
      rAccountName: [null],
      bookkeepingPayType: [null],
      groupIds: [null],
      control: [null],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const bookkeepingTemplate: BookkeepingTemplate = this.getRawValue();
    bookkeepingTemplate.gateway = getRelation(this.editForm.get('gateway').value);
    bookkeepingTemplate.recipient = getRelation(this.editForm.get('recipient').value);
    this.save(bookkeepingTemplate);
  }
}
