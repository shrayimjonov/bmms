import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlType, Field } from './field.model';
import { FieldType } from '../../../shared/model/enumerations/field-type.model';
import { RequestType } from '../../../shared/model/enumerations/request-type.model';
import { Visibility } from '../../../shared/model/enumerations/visibility.model';
import { Validators } from '@angular/forms';
import { PrefixService } from '../prefix/prefix.service';
import { FieldService } from './field.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { debounceTime, pluck, takeUntil } from 'rxjs/operators';
import { RecipientService } from '../recipient/recipient.service';
import { Prefix } from '../prefix/prefix.model';
import { FieldValueService } from '../field-value/field-value.service';
import { FieldValue } from '../field-value/field-value.model';
import { Event } from '../../../config/event.constants';
import { getRelation } from '../../../shared/functions';
import { ONLY_LATIN_REGEX_VALIDATOR } from '../../../config/input-validator.constants';
import { Authority } from '../../../config/authority.constants';
import { EntityUpdateBaseComponent } from '../../../shared/entity/entity-update-base-component';
import { EventManager } from '../../../core/util/event-manager.service';

@Component({
  templateUrl: 'field-update-modal.template.html',
})
export class FieldUpdateModalComponent extends EntityUpdateBaseComponent<Field, FieldService> implements OnInit, OnDestroy {
  readonly authority = Authority;
  prefixes: Prefix[];
  fieldValues: FieldValue[];

  readonly contents = ['NUMBER', 'TEXT'];
  readonly types = Object.keys(FieldType);
  readonly requestTypes = Object.keys(RequestType);
  readonly controlTypes = Object.keys(ControlType);
  readonly visibilities = Object.keys(Visibility);

  constructor(
    public readonly recipientService: RecipientService,
    public readonly prefixService: PrefixService,
    public readonly fieldValueService: FieldValueService,
    private readonly activeModal: NgbActiveModal,
    private readonly fieldService: FieldService,
    private readonly eventManager: EventManager
  ) {
    super(activeModal, fieldService, Event.FIELD_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.pattern(ONLY_LATIN_REGEX_VALIDATOR)]],
      extId: [null, Validators.required],
      type: [null, Validators.required],
      controlType: [null, Validators.required],
      content: ['TEXT'],
      validation: [null, Validators.maxLength(128)],
      placeholder: [null, Validators.maxLength(128)],
      mask: [null, Validators.maxLength(256)],
      maskSymbol: [null, Validators.maxLength(2)],
      lengthMin: [null, [Validators.minLength(0), Validators.maxLength(256)]],
      lengthMax: [null, [Validators.minLength(0), Validators.maxLength(256)]],
      requireField: [null, Validators.maxLength(64)],
      filterBy: [null, Validators.maxLength(16)],
      optional: [true],
      output: [true],
      visibility: [null],
      ord: [null],
      requestType: [null],
      recipientShortName: [null],
      recipient: [null, Validators.required],
      titles: [null, Validators.required],
      descriptions: [null, Validators.required],
      values: [null],
      prefixes: [null],
      fromRequest: [false],
      fromResponse: [false],
    });

    this.loadPrefixes();
    this.loadFieldValues();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private loadPrefixes(): void {
    this.prefixService
      .query()
      .pipe(pluck('body'), takeUntil(this.destroy$))
      .subscribe((prefixes: Prefix[]) => (this.prefixes = prefixes));
  }

  private loadFieldValues(): void {
    this.fieldValueService
      .query()
      .pipe(pluck('body'), takeUntil(this.destroy$))
      .subscribe((values: FieldValue[]) => (this.fieldValues = values));
  }

  onSubmit(): void {
    const field: Field = this.getRawValue();
    field.recipient = getRelation(this.editForm.get('recipient').value);
    this.save(field);
  }

  readonly comparePrefixes = (o1: Prefix, o2: Prefix): boolean => (o1 && o2 ? o1.id === o2.id : o1 === o2);

  readonly compareFieldValues = (o1: FieldValue, o2: FieldValue): boolean => (o1 && o2 ? o1.id === o2.id : o1 === o2);

  registerToPrefixModify(value: any): void {
    // TODO: add to subscription
    this.eventManager.subscribe(Event.PREFIX_LIST_MODIFICATION, () => {
      const query = {
        'value.contains': value,
      };
      this.prefixService
        .query(query)
        .pipe(takeUntil(this.destroy$), debounceTime(1000))
        .subscribe(res => (this.prefixes = res.body));
    });
  }

  loadFieldValuesModification(value: any): void {
    // TODO: add to subscription
    this.eventManager.subscribe(Event.FIELD_VALUE_LIST_MODIFICATION, () => {
      const query = {
        'value.contains': value,
      };
      this.fieldValueService
        .query(query)
        .pipe(takeUntil(this.destroy$), debounceTime(1000))
        .subscribe(res => (this.fieldValues = res.body));
    });
  }
}
