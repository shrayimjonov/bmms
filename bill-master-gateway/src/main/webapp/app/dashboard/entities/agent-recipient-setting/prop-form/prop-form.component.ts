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
import {Prop} from './prop-form.model';
import {AgentRecipientSettingService} from '../agent-recipient-setting.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EposExpressionCheckerModalComponent} from './epos-expression-checker-modal/epos-expression-checker-modal.component';

@Component({
  selector: 'prop-form',
  templateUrl: 'prop-form.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => PropFormComponent),
      multi: true,
    },
  ]
})
export class PropFormComponent implements ControlValueAccessor, OnInit {
  array = this.fb.array([], Validators.required);
  keys = Object.keys(Prop);
  prop = Prop;

  constructor(
    private readonly fb: FormBuilder,
    private readonly modalService: NgbModal,
    private readonly agentRecipientSettingService: AgentRecipientSettingService
  ) {}

  ngOnInit(): void {
    this.array.valueChanges.pipe(distinct()).subscribe(() => this.emitChanges());
  }

  onChange: (prop: any) => void = () => {};

  onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(prop: any): void {
    if (prop) {
      Object.keys(prop).forEach(key => {
        this.array.push(
          this.fb.group({
            label: key,
            name: prop[key].value,
          })
        );
      });
    } else {
      this.push();
    }
  }

  isUsed(key: string): boolean {
    return this.array.controls.some(control => control.get('label')?.value === key);
  }

  canPush(index: number): boolean {
    return this.array.length === index + 1;
  }

  convertGroup(group: AbstractControl): FormGroup {
    return group as FormGroup;
  }

  submit(): void {
    if (this.array.valid) {
      this.push();
    }
  }

  openExpressionChecker(expression: string): void {
    const modal = this.modalService.open(EposExpressionCheckerModalComponent, { size: 'lg', backdrop: 'static' });
    modal.componentInstance.request = {
      expression: expression,
    };
  }

  push(): void {
    const group = this.fb.group({
      label: [null, Validators.required],
      name: [null, Validators.required],
    });

    this.array.push(group);
    this.focus(this.array.length - 1);
  }

  remove(index: number): void {
    this.array.removeAt(index);
  }

  focus(index: number): void {
    setTimeout(() => document.getElementById(`label-${index}`)?.focus());
  }

  private emitChanges(): void {
    const result = {};
    this.array.controls
      .filter((control: AbstractControl) => control.valid)
      .forEach(validControl => {
        const key = validControl.get('label')?.value;
        result[key] = {
          key: key,
          value: validControl.get('name')?.value,
        };
      });

    this.onChange(result);
  }
}
