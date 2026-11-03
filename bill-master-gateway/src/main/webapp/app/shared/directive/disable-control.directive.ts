import { NgControl } from '@angular/forms';
import { Directive, Input, OnChanges, SimpleChanges } from '@angular/core';

@Directive({
  selector: '[disable]',
})
export class DisableControlDirective implements OnChanges {
  @Input() disable: boolean;

  constructor(private control: NgControl) {}

  ngOnChanges(changes: SimpleChanges): void {
    const action = changes.disable.currentValue ? 'disable' : 'enable';
    this.control.control[action]({ emitEvent: false });
  }
}
