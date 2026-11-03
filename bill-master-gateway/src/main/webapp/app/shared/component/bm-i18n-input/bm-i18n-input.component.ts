import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  forwardRef,
  Input,
  ViewChild
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, NgForm} from '@angular/forms';
import {Lang} from '../../../config/language.constants';
import {I18n} from '../../model/pkbillms/i18n.model';

@Component({
  selector: 'bm-i18n-input',
  templateUrl: 'bm-i18n-input.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [
    `
      :host {
        border: unset !important;
      }
    `,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => BmI18nInputComponent),
      multi: true,
    },
  ],
})
export class BmI18nInputComponent implements ControlValueAccessor, AfterViewInit {
  @Input() duplicate: boolean;
  @ViewChild('editForm', { read: NgForm }) editForm: NgForm;
  i18ns: any;
  languages = Object.keys(Lang);

  constructor(private hostElement: ElementRef) {}

  ngAfterViewInit(): void {
    this.editForm.valueChanges.subscribe(() => {
      if (this.editForm.invalid) {
        this.onChange(null);
      } else {
        this.onChange(this.i18ns);
      }
    });
  }

  get disabled(): boolean {
    return this.hostElement.nativeElement.disabled;
  }

  onChange: (i18ns: any) => void = () => {};

  private onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(i18ns: I18n[]): void {
    this.i18ns = this.languages.map<I18n>(lang => {
      const i18n = i18ns?.find(el => el.lang === lang);

      return {
        id: this.duplicate ? null : i18n?.id,
        text: i18n?.text,
        lang: Lang[lang],
      };
    });
  }

  setDisabledState(isDisabled: boolean): void {
    this.hostElement.nativeElement.disabled = isDisabled;
  }
}
