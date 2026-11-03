import {Component, Input} from '@angular/core';

@Component({
  selector: 'sort-icon',
  template: `
    <i nz-icon nzType="{{icon}}" nzTheme="outline"></i>
  `,
  styles: [`
    :host {
      float: right;
    }
  `]
})
export class SortIconComponent {
  @Input() icon = 'sort';
}
