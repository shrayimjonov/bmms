import {ChangeDetectionStrategy, Component, forwardRef, Input, OnDestroy} from '@angular/core';
import {BmFile} from './bm-file.model';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {Subscription} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {Util} from "../../util";

@Component({
  selector: 'bm-file',
  templateUrl: 'bm-file.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      // eslint-disable-next-line @typescript-eslint/no-use-before-define
      useExisting: forwardRef(() => BmFileComponent),
      multi: true,
    },
  ],
})
export class BmFileComponent implements ControlValueAccessor, OnDestroy {
  @Input() bmAccept: string | null = 'image/*';

  private _file: BmFile | null;
  private subscription?: Subscription;

  loading: boolean;

  onSelected(event: any): void {
    const file: File = event.target.files[0];

    if (file) {
      this.loadFile(file);
    } else {
      this.file = null;
    }
  }

  private loadFile(file: File): void {
    this.loading = true;
    this.subscription = Util.readAsDataUrl(file)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe((bmFile: BmFile | null) => (this.file = bmFile));
  }

  delete(fileInpRef: HTMLInputElement): void {
    fileInpRef.value = null;
    this.file = null;
  }

  getImageSrc(): string {
    const contentType = this.file.contentType;

    if (['image/jpeg', 'image/png'].includes(contentType)) {
      return `data:${contentType};base64,${this.file.content}`;
    }

    // if (['text/plain'].includes(contentType)) {
    //   return '../../../../content/images/no_image.png'
    // }

    return '../../../../content/images/no_image.png';
  }

  set file(file: BmFile | null) {
    this._file = file;
    this.onChange(file);
  }

  get file(): BmFile | null {
    return this._file;
  }

  onChange: (file: BmFile | null) => void = () => {
  };

  private onTouched = () => {
  };

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(file: BmFile): void {
    this._file = file;
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
