import {Component, OnDestroy, OnInit} from '@angular/core';
import {Prefix} from './prefix.model';
import {Validators} from '@angular/forms';
import {PrefixService} from './prefix.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {pluck, takeUntil} from 'rxjs/operators';
import {PrefixType} from '../../../shared/model/enumerations/prefix-type.model';
import {FieldService} from '../field/field.service';
import {Field} from '../field/field.model';
import {Event} from '../../../config/event.constants';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'prefix-update-modal.template.html',
})
export class PrefixUpdateModalComponent extends EntityUpdateBaseComponent<Prefix, PrefixService> implements OnInit, OnDestroy {
  readonly type = Object.keys(PrefixType);
  fields: Field[];

  constructor(public readonly prefixService: PrefixService,
              private readonly fieldService: FieldService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, prefixService, Event.PREFIX_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      type: [null, Validators.required],
      value: [null, Validators.maxLength(16)],
      output: [false],
      requireField: [null, Validators.maxLength(64)],
      field: [null, Validators.required],
    });

    this.loadFields();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private loadFields(): void {
    this.fieldService.query()
      .pipe(
        pluck('body'),
        takeUntil(this.destroy$)
      )
      .subscribe((fields: Field[]) => this.fields = fields);
  }

  compareFn = (o1: Field, o2: Field): boolean => {
    if (!o1 || !o2) {
      return false;
    }

    return o1.id === o2.id;
  };

  onSubmit(): void {
    const prefix: Prefix = this.getRawValue();
    prefix.field = getRelation(this.editForm.get('field').value);

    this.save(prefix);
  }
}
