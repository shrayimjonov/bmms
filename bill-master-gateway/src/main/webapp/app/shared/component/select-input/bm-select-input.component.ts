import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  forwardRef,
  Input,
  Output,
  Renderer2
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {NzSelectModeType} from 'ng-zorro-antd/select';
import {finalize, takeUntil} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {Subject} from 'rxjs';
import {ITEMS_PER_PAGE} from '../../../config/pagination.constants';

@Component({
  selector: 'bm-select-input',
  templateUrl: 'bm-select-input.template.html',
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
      useExisting: forwardRef(() => BmSelectInputComponent),
      multi: true,
    },
  ],
})
export class BmSelectInputComponent implements ControlValueAccessor, AfterViewChecked {
  @Input() bmLabelField: string;
  @Input() bmValueField: string;
  @Input() bmMode: NzSelectModeType = 'default';
  @Input() bmMaxTagCount = 0;
  @Input() bmOptionOverflowSize = 8;
  @Input() bmShowSearch: boolean;
  @Input() bmAllowClear = true;
  @Input() bmServerSearch: boolean;
  @Input() data: any[];
  @Input() requestUrl: string;

  @Output() bmOnSelect = new EventEmitter();
  @Output() bmOnSearch = new EventEmitter<string>();

  private readonly destroy$ = new Subject<void>();

  disabled: boolean;
  item: unknown;
  loading: boolean;

  constructor(private readonly elementRef: ElementRef,
              private readonly http: HttpClient,
              private readonly cdr: ChangeDetectorRef,
              private readonly renderer: Renderer2) {}

  ngAfterViewChecked(): void {
    this.updateBorder();
  }

  private updateBorder(): void {
    const nzInput = (this.elementRef.nativeElement.childNodes as NodeList).item(0)?.firstChild;
    this.renderer.setStyle(nzInput, 'border', this.elementRef.nativeElement.style?.border);
  }

  onChange: (v: unknown) => void = () => {};

  private onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(v: any): void {
    this.item = this.getInitialValue(v);
  }

  private getInitialValue(v: any): any {
    if (v) {
      if (this.requestUrl) {
        this.onSearch(v[this.bmLabelField]);
      }
      return v;
    }

    if (this.bmMode === 'default') {
      return {};
    }

    return [];
  }

  emitValue(): void {
    this.onChange(this.item);
    this.bmOnSelect.emit(this.item);
  }

  onSearch(text: string): void {
    this.bmOnSearch.emit(text);

    if (!this.bmServerSearch) return;

    const query = {
      size: ITEMS_PER_PAGE,
    };

    if (text) {
      query[`${this.bmLabelField}.contains`] = text;
    }

    if (!this.requestUrl) {
      throw new Error(`Property 'resourceUrl' does not set`);
    }

    this.loading = true;
    this.http
      .get(this.requestUrl, {params: query, observe: 'body'})
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe((data: any[]) => (this.data = data));
  }

  compareFn = (o1: any, o2: any): boolean => {
    if (!o1 || !o2) {
      return false;
    }

    if (this.bmLabelField && !this.bmValueField) {
      return o1[this.bmLabelField] === o2[this.bmLabelField];
    }

    return o1 === o2;
  };

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
