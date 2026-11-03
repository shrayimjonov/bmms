import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'bm-amount-input',
  templateUrl: 'bm-amount-input.template.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => BmAmountInputComponent),
      multi: true,
    },
  ],
})
export class BmAmountInputComponent implements ControlValueAccessor {
  @Input() ratio: number;
  @Input() placeholder: string;

  amount: number;

  onChange: (amount: number) => void = () => {
  };

  private onTouched = () => {
  };

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(amount: number): void {
    this.amount = amount;
  }
}
