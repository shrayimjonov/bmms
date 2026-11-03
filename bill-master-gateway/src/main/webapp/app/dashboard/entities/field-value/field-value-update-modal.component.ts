import {Component, OnDestroy, OnInit} from '@angular/core';
import {FieldValue} from './field-value.model';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FieldValueService} from './field-value.service';
import {Field} from '../field/field.model';
import {FieldService} from '../field/field.service';
import {HttpResponse} from '@angular/common/http';
import {ITEMS_PER_PAGE} from '../../../config/pagination.constants';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";
import {Event} from "../../../config/event.constants";

@Component({
  templateUrl: 'field-value-update-modal.template.html',
})
export class FieldValueUpdateModalComponent extends EntityUpdateBaseComponent<FieldValue, FieldValueService> implements OnInit, OnDestroy {
  fields: Field[];

  constructor(private readonly fieldValueService: FieldValueService,
              private readonly fieldService: FieldService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, fieldValueService, Event.FIELD_VALUE_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      value: [null, [Validators.required, Validators.maxLength(64)]],
      amount: [null],
      filterBy: [null, Validators.maxLength(16)],
      ord: [],
      field: [null, Validators.required],
      titles: [null, Validators.required],
      descriptions: [],
    });

    this.onFieldSearch(null);
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  compareFn = (o1: Field, o2: Field): boolean => {
    if (!o1 || !o2) {
      return false;
    }

    return o1.id === o2.id;
  };

  onFieldSearch(text: string): void {
    const query = {
      size: ITEMS_PER_PAGE,
    };

    if (text) {
      query['name.contains'] = text;
    }

    this.fieldService.query(query).subscribe((res: HttpResponse<Field[]>) => (this.fields = res.body));
  }

  onSubmit(): void {
    const fieldValue: FieldValue = this.getRawValue();
    fieldValue.field = getRelation(this.editForm.get('field').value);

    if (fieldValue.amount) {
      fieldValue.amount = Number(fieldValue.amount);
    }

    this.save(fieldValue);
  }
}
