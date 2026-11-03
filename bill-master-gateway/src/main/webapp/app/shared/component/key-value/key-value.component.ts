import {ChangeDetectionStrategy, Component, forwardRef, OnInit} from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
  Validators
} from '@angular/forms';
import {distinct} from 'rxjs/operators';

@Component({
  selector: 'key-value',
  templateUrl: 'key-value.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => KeyValueComponent),
      multi: true,
    },
  ],
})
export class KeyValueComponent implements ControlValueAccessor, OnInit {
  array = this.fb.array([], Validators.required);

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.array.valueChanges.pipe(distinct()).subscribe(() => this.emitData());
  }

  onChange: (obj: any) => void = () => {};

  onTouched = () => {};

  convertGroup(group): FormGroup {
    return group as FormGroup;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(obj: any): void {
    this.array.clear({ emitEvent: false });
    if (obj) {
      Object.keys(obj).forEach(key => this.push(key, obj[key]));
    } else {
      this.push();
    }
  }

  canPush(index: number): boolean {
    return this.array.length === index + 1;
  }

  submit(): void {
    if (this.array.valid) {
      this.push();
      this.focus(this.array.length - 1);
    }
  }

  push(key = null, value = null): void {
    const group = this.fb.group({
      key: [key, Validators.required],
      value: [value, Validators.required],
    });

    this.array.push(group);
  }

  remove(index: number): void {
    this.array.removeAt(index);
  }

  focus(index: number): void {
    setTimeout(() => document.getElementById(`key-${index}`)?.focus());
  }

  private emitData(): void {
    const filtered = this.array.controls.filter((control: AbstractControl) => control.valid);

    if (!filtered.length) {
      return this.onChange(null);
    }

    const result = {};
    filtered.forEach(
      (filteredControl: AbstractControl) => (result[filteredControl.get('key')?.value] = filteredControl.get('value')?.value)
    );

    this.onChange(result);
  }
}
