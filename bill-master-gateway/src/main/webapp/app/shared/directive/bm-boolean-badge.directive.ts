import {Directive, ElementRef, Input} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

export type BmBooleanBadgeType = 'boolean' | 'success' | 'active' | string;

@Directive({
  selector: '[bm-boolean-badge]',
})
export class BmBooleanBadgeDirective {
  @Input() bmType: BmBooleanBadgeType;

  badgeMap = new Map<boolean, string>([
    [true, 'badge-success'],
    [false, 'badge-danger'],
    [null, 'badge-danger'],
    [undefined, 'badge-danger']
  ]);

  constructor(private elRef: ElementRef<HTMLElement>,
              private translateService: TranslateService) {
  }

  @Input('bm-boolean-badge')
  set updateValue(value: boolean) {
    if (this.bmType) {
      this.elRef.nativeElement.innerText = this.translateService.instant(`global.${this.bmType}.${value}`);
    }
    this.elRef.nativeElement.className = `badge ${this.badgeMap.get(value)}`;
  }
}
