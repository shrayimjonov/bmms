import { Directive, ElementRef, Input, OnDestroy, OnInit } from '@angular/core';
import { I18n } from '../../model/pkbillms/i18n.model';
import { Subject } from 'rxjs';
import { BmI18nService } from './bm-i18n.service';
import { takeUntil } from 'rxjs/operators';
import { LanguageService } from '../../../core/util/language.service';

@Directive({
  selector: '[bm-i18n]',
})
export class BmI18nDirective implements OnInit, OnDestroy {
  @Input('bm-i18n') i18ns: I18n[];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly elementRef: ElementRef<HTMLElement>,
    private readonly langService: LanguageService,
    private readonly bmI18nService: BmI18nService
  ) {}

  ngOnInit(): void {
    this.updateText();
    this.registerOnChangeLang();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateText(): void {
    if (!this.i18ns?.length) {
      return null;
    }

    this.elementRef.nativeElement.innerText = this.bmI18nService.getText(this.i18ns);
  }

  private registerOnChangeLang(): void {
    this.langService
      .onChange()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.updateText());
  }
}
