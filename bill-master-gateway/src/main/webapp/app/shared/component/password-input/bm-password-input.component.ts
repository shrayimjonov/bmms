import {AfterViewChecked, Component, ElementRef, forwardRef, Input, Renderer2} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
  selector: 'bm-password-input',
  templateUrl: 'bm-password-input.template.html',
  styles: [`
    :host {
      border: unset !important;
    }
  `],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => BmPasswordInputComponent),
      multi: true
    }
  ]
})
export class BmPasswordInputComponent implements ControlValueAccessor, AfterViewChecked {
  @Input() placeholder: string;

  private _password: string;

  disabled: boolean;
  isVisible: boolean;

  constructor(private elementRef: ElementRef,
              private renderer: Renderer2) {
  }

  ngAfterViewChecked(): void {
    this.updateBorder();
  }

  private updateBorder(): void {
    const nzInputGroup = (this.elementRef.nativeElement.childNodes as NodeList).item(0);
    this.renderer.setStyle(nzInputGroup, 'border', this.elementRef.nativeElement.style?.border);
  }

  set password(password: string) {
    this._password = password;
    this.onChange(password);
  }

  get password(): string {
    return this._password;
  }

  onChange: (password: string) => void = () => {
  };

  private onTouched = () => {
  };

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(password: string): void {
    this._password = password;
  }

  getInputType(): string {
    return this.isVisible ? 'text' : 'password';
  }

  getIconType(): string {
    return this.isVisible ? 'eye-invisible' : 'eye';
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
