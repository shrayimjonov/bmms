import {Component, OnDestroy, OnInit} from '@angular/core';
import {ResponseField, ResponseFieldType} from './response-field.model';
import {Visibility} from '../../../shared/model/enumerations/visibility.model';
import {RequestType} from '../../../shared/model/enumerations/request-type.model';
import {Validators} from '@angular/forms';
import {ResponseFieldService} from './response-field.service';
import {RecipientService} from '../recipient/recipient.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../config/event.constants';
import {getRelation} from '../../../shared/functions';
import {AMOUNT_RATIOS} from '../../../config/pagination.constants';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'response-field-update-modal.template.html',
})
export class ResponseFieldUpdateModalComponent extends EntityUpdateBaseComponent<ResponseField, ResponseFieldService> implements OnInit, OnDestroy {
  readonly visibilities = Object.keys(Visibility);
  readonly requestTypes = Object.keys(RequestType);
  readonly types = Object.keys(ResponseFieldType);
  readonly amountRatios = AMOUNT_RATIOS;

  constructor(public readonly recipientService: RecipientService,
              private readonly activeModal: NgbActiveModal,
              private readonly responseFieldService: ResponseFieldService) {
    super(activeModal, responseFieldService, Event.RESPONSE_FIELD_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(128)]],
      visibility: [],
      ord: [],
      requestType: [],
      recipient: [null, Validators.required],
      labels: [null, Validators.required],
      type: [null],
      amountRatio: [this.amountRatios[0].value, Validators.required],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  isTypeOfAmount(): boolean {
    return this.editForm.get('type')?.value === ResponseFieldType.AMOUNT;
  }

  onSubmit(): void {
    const responseField: ResponseField = this.getRawValue();
    responseField.recipient = getRelation(this.editForm.get('recipient').value);
    this.save(responseField);
  }
}
