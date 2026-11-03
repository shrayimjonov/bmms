import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {CurrencyService} from './currency.service';
import {Currency, CurrencyGroup} from './currency.model';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../config/event.constants';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'currency-update.template.html',
})
export class CurrencyUpdateComponent extends EntityUpdateBaseComponent<Currency, CurrencyService> implements OnInit, OnDestroy {
  readonly groups = Object.keys(CurrencyGroup);

  constructor(private readonly currencyService: CurrencyService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, currencyService, Event.CURRENCY_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      code: [null, [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
      ccy: [null, [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
      nameRu: [null, [Validators.maxLength(64)]],
      rate: [],
      sellRate: [],
      buyRate: [],
      date: [],
      lastModifiedDate: [],
      active: [null, [Validators.required]],
      ccyGroup: [null, [Validators.maxLength(32)]],
      otherCcyGroup: [null],
    })

    // if (this.data.id) {
    //   this.editForm.patchValue({
    //     date: this.data.date ? dayjs(this.data.date).toDate() : null,
    //   }, {emitEvent: false});
    // }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    // const date = dayjs((this.editForm.get('date')?.value as Date).setHours(10));
    const currency: Currency = this.getRawValue();
    // currency.date = dayjs(date, {utc: true});
    currency.ccyGroup = this.getCcyGroupValue();

    this.save(currency);
  }

  onCcyGroupChange(): void {
    this.editForm.get('otherCcyGroup').reset();

    if (this.isOther()) {
      setTimeout(() => document.getElementById('field_otherCcyGroup')?.focus());
    }
  }

  isOther(): boolean {
    return this.editForm.get('ccyGroup')?.value === CurrencyGroup.OTHER;
  }

  private getCcyGroupValue(): CurrencyGroup | string {
    const group = this.editForm.get('ccyGroup')?.value as CurrencyGroup;

    if (CurrencyGroup.DEFAULT_GROUP === group) {
      return '';
    }

    if (this.isOther()) {
      return this.editForm.get('otherCcyGroup')?.value;
    }

    return group;
  }
}
