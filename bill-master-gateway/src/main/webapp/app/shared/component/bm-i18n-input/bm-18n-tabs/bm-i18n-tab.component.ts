import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  forwardRef,
  Input,
  ViewChild
} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR, NgForm} from "@angular/forms";
import {Lang} from "../../../../config/language.constants";
import {I18n} from "../../../model/pkbillms/i18n.model";

@Component({
  selector: 'bm-i18n-tab',
  templateUrl: 'bm-i18n-tab.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrls: ['bm-i18n-tab.style.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => BmI18nTabComponent),
      multi: true,
    },
  ],
})
export class BmI18nTabComponent implements ControlValueAccessor, AfterViewInit {
  @Input() duplicate: boolean;
  @ViewChild('editForm', {read: NgForm}) editForm: NgForm;
  i18ns: any;
  languages = Object.keys(Lang);

  constructor(private hostElement: ElementRef) {
  }

  ngAfterViewInit(): void {
    this.editForm.valueChanges.subscribe(() => {
      if (this.editForm.invalid) {
        this.onChange(null);
      } else {
        // this.i18ns.forEach(i18n => i18n.editor = JSON.stringify(i18n.editor))
        this.onChange(this.i18ns);
      }
    });
  }

  get disabled(): boolean {
    return this.hostElement.nativeElement.disabled;
  }

  onChange: (i18ns: any) => void = () => {
  };

  private onTouched = () => {
  };

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
        lang: Lang[lang]
      };
    });
  }

  setDisabledState(isDisabled: boolean): void {
    this.hostElement.nativeElement.disabled = isDisabled;
  }

  focus = (() => document.getElementById("text-area").focus())
}
