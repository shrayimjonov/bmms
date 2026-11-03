import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {JsonEditorOptions} from 'ang-jsoneditor';

@Component({
  selector: 'bm-json-viewer',
  template: `<json-editor [options]="options" [(ngModel)]="data"></json-editor>`,
  styleUrls: ['bm-json-viewer.style.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BmJsonViewerComponent implements OnInit {
  @Input() data: any;
  options: JsonEditorOptions;

  ngOnInit(): void {
    this.options = new JsonEditorOptions();
    this.options.mainMenuBar = false;
    this.options.navigationBar = false;
    this.options.statusBar = false;
    this.options.expandAll = true;
    this.options.mode = 'view';

    this.data = typeof this.data === 'string' ? JSON.parse(this.data) : this.data;
  }
}
