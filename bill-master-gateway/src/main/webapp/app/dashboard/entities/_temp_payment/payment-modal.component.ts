import {Component, OnDestroy, OnInit} from '@angular/core';
import {RecipientProxy} from './model/recipient-proxy.model';
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {Field} from '../field/field.model';
import {FieldType} from '../../../shared/model/enumerations/field-type.model';
import {FieldValue} from '../field-value/field-value.model';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {CheckRequest} from '../../../shared/model/pkbillms/check-request.model';
import {SimpleBillResponse} from '../../../shared/model/pkbillms/simple-bill-response.model';
import {PaymentService} from './payment.service';
import {Subject} from 'rxjs';
import {finalize, takeUntil} from 'rxjs/operators';
import {CheckoutParams} from '../../../checkout/checkout.model';
import {fadeIn} from '../../../shared/animations';

@Component({
  templateUrl: 'payment-modal.component.html',
  animations: [fadeIn],
})
export class PaymentModalComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  readonly recipient: RecipientProxy;
  readonly categoryName: string;
  readonly lang: string;
  readonly type = FieldType;
  readonly editForm = new FormGroup({});

  loading: boolean;

  constructor(
    public readonly activeModal: NgbActiveModal,
    public readonly paymentService: PaymentService,
    private readonly fb: FormBuilder,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.recipient.fields.forEach(field => {
      const control: AbstractControl = this.fb.control(null);
      const validators = this.getFieldValidators(field);
      control.setValidators(validators);
      this.editForm.addControl(field.name, control);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private getFieldValidators(field: Field): ValidatorFn[] {
    const validators: ValidatorFn | ValidatorFn[] = [];

    if (!field.optional) {
      validators.push(Validators.required);
    }

    if (field.lengthMin) {
      validators.push(Validators.minLength(field.lengthMin));
    }

    if (field.lengthMax) {
      validators.push(Validators.maxLength(field.lengthMax));
    }

    if (field.type === FieldType.AMOUNT) {
      validators.push(Validators.min(this.recipient.minAmount / 100), Validators.max(this.recipient.maxAmount / 100));
    }

    if (field.validation) {
      validators.push(Validators.pattern(field.validation));
    }

    return validators;
  }

  save(): void {
    const request: CheckRequest = {
      recipientId: this.recipient.id,
      amount: this.getAmount(),
      account: this.getPayAccount(),
      params: this.getPayParams(),
    };

    this.check(request);
  }

  private getAmount(): number | null {
    const field = this.recipient.fields.find(f => f.type === FieldType.AMOUNT);

    if (!field) {
      return null;
    }

    return this.editForm.get(field.name).value * 100;
  }

  private getPayAccount(): string | null {
    const field = this.recipient.fields.find(f => f.type === FieldType.ACCOUNT);

    if (!field) {
      return null;
    }

    return this.editForm.get(field.name).value;
  }

  private getPayParams(): {} {
    const result = {};
    this.recipient.fields
      .filter(f => f.type === FieldType.OTHER)
      .forEach(field => (result[field.name] = this.editForm.get(field.name).value));

    return result;
  }

  private check(request: CheckRequest): void {
    this.loading = true;
    this.paymentService
      .check(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((response: SimpleBillResponse) => this.onChecked(response));
  }

  private onChecked(response: SimpleBillResponse): void {
    if (!response.success) {
      return this.paymentService.showResponseExceptionMessage(response);
    }

    return this.navigateToCheckout(response);
  }

  private navigateToCheckout(response: SimpleBillResponse): void {
    const checkout: CheckoutParams = {
      recipientId: this.recipient.id,
      amount: response.amount,
      account: response.account,
      orderId: response.account,
      billId: response.id,
    };

    this.router.navigate(['checkout'], { queryParams: checkout }).then(() => this.activeModal.dismiss('close'));
  }

  getMin(field: Field): number {
    if (field.type === FieldType.AMOUNT) {
      return this.recipient.minAmount;
    }

    return field.lengthMin;
  }

  getMax(field: Field): number {
    if (field.type === FieldType.AMOUNT) {
      return this.recipient.maxAmount;
    }

    return field.lengthMax;
  }

  hasRequiredField(field: Field): boolean {
    return !field.requireField || (field.requireField && this.editForm.get(field.requireField)?.value);
  }

  hasError(field: Field): boolean {
    return this.editForm.get(field.name)!.invalid && (this.editForm.get(field.name)!.dirty || this.editForm.get(field.name)!.touched);
  }

  hideNotRequiredFieldValue(requireField: string, fieldValue: FieldValue): boolean {
    return requireField && this.editForm.get(requireField)?.value !== fieldValue.filterBy;
  }

  resetRequiredFieldValue(field: Field): void {
    this.recipient.fields.forEach(f => {
      if (f.requireField === field.name) {
        this.editForm.get(f.name)?.reset({ emitEvent: false });
      }
    });
  }
}
