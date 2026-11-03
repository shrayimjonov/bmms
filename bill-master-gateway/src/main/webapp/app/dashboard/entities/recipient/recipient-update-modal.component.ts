import { Component, OnDestroy, OnInit } from '@angular/core';
import { Validators } from '@angular/forms';
import { debounceTime, finalize, takeUntil } from 'rxjs/operators';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { RecipientService } from './recipient.service';
import { GatewayService } from '../gateway/gateway.service';
import { CategoryService } from '../category/category.service';
import { Recipient, RecipientType } from './recipient.model';
import { Field } from '../field/field.model';
import { FieldService } from '../field/field.service';
import { Event } from '../../../config/event.constants';
import { getJsonEditorOptions, getRelation, getRelations } from '../../../shared/functions';
import { AMOUNT_RATIOS, CURRENCIES, ITEMS_PER_PAGE } from '../../../config/pagination.constants';
import { LOGIN_REGEX_VALIDATOR } from '../../../config/input-validator.constants';
import { EntityUpdateBaseComponent } from '../../../shared/entity/entity-update-base-component';
import { EnumerationService } from '../../../shared/service/enumeration/enumeration.service';

@Component({
  templateUrl: 'recipient-update-modal.template.html',
})
export class RecipientUpdateModalComponent extends EntityUpdateBaseComponent<Recipient, RecipientService> implements OnInit, OnDestroy {
  readonly editorOptions = getJsonEditorOptions();

  fields: Field[] = [];
  fieldIsLoading: boolean;

  types: string[];
  readonly currencies = CURRENCIES;
  readonly amountRatios = AMOUNT_RATIOS;

  readonly actions = [
    { label: 'Активность', field: 'active' },
    { label: 'Проверка', field: 'check' },
    { label: 'Оплата', field: 'pay' },
    { label: 'Статус', field: 'status' },
    { label: 'Отмена', field: 'cancel' },
    { label: 'Депозит', field: 'canDeposit' },
    { label: 'Сверка', field: 'reconcile' },
  ];

  constructor(
    public readonly categoryService: CategoryService,
    public readonly gatewayService: GatewayService,
    private readonly recipientService: RecipientService,
    private readonly activeModal: NgbActiveModal,
    private readonly fieldService: FieldService,
    private readonly enumService: EnumerationService
  ) {
    super(activeModal, recipientService, Event.RECIPIENT_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      shortName: [null, [Validators.required, Validators.maxLength(64), Validators.pattern(LOGIN_REGEX_VALIDATOR)]],
      serviceId: [null, [Validators.required, Validators.maxLength(64)]],
      logo: [null, [Validators.maxLength(64)]],
      amountRatio: [this.amountRatios[0].value, [Validators.required]],
      ccy: [this.currencies[0].value, [Validators.required]],
      limitRatio: [this.amountRatios[0].value, [Validators.required]],
      minAmount: [null],
      maxAmount: [null],
      commission: [null],
      fee: [],
      systemCommission: [],
      systemFee: [],
      senderCommission: [],
      ord: [],
      active: [true],
      check: [false],
      pay: [false],
      status: [false],
      cancel: [false],
      canDeposit: [false],
      reconcile: [false],
      mfo: [null, [Validators.maxLength(5)]],
      inn: [null, [Validators.maxLength(9)]],
      gateway: [null],
      category: [null],
      titles: [null, Validators.required],
      fullNames: [null, Validators.required],
      phoneNumber: [null],
      fields: [null],
      settlementAccount: [null],
      type: [null],
      jsonParams: [null],
    });

    if (this.data.id) {
      this.editForm.patchValue({
        jsonParams: JSON.parse(this.data.jsonParams),
      });
    }

    this.onFieldSearch(null, !!this.data.id);
    this.loadTypes();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private loadTypes(): void {
    this.enumService
      .getOnlyValues('RecipientType', RecipientType)
      .pipe(takeUntil(this.destroy$))
      .subscribe((types: string[]) => (this.types = types));
  }

  isUpdate(): boolean {
    return !!this.editForm.get('id')?.value;
  }

  compareFields = (o1: Field, o2: Field): boolean => (o1 && o2 ? o1.id === o2.id : o1 === o2);

  onSubmit(): void {
    const recipient: Recipient = this.getRawValue();
    recipient.createClient = true;
    recipient.gateway = getRelation(this.editForm.get('gateway').value);
    recipient.category = getRelation(this.editForm.get('category').value);
    recipient.fields = getRelations(this.editForm.get('fields').value);
    recipient.jsonParams = JSON.stringify(this.editForm.get('jsonParams')?.value);

    this.save(recipient, null, () => {
      const fields: Field[] = this.editForm.get('fields').value;

      if (fields) {
        fields.forEach(field => (field.recipient.id = recipient.id));
        this.fieldService.updateList(fields).pipe(takeUntil(this.destroy$)).subscribe();
      }
    });
  }

  onFieldSearch(name: string, hasSavedFields?: boolean): void {
    const query = {
      size: ITEMS_PER_PAGE,
    };

    if (name) {
      query['name.contains'] = name;
    }

    if (hasSavedFields && this.data.id) {
      query['recipientId.equals'] = this.data.id;
    }

    this.fieldIsLoading = true;
    this.fieldService
      .query(query)
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(1000),
        finalize(() => (this.fieldIsLoading = false))
      )
      .subscribe(res => (this.fields = res.body));
  }
}
