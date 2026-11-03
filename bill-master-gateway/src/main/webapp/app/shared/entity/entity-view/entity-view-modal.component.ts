import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {EntityViewOptions} from './entity-view-options.model';

@Component({
  templateUrl: 'entity-view-modal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityViewModalComponent implements OnInit {
  @Input() id: number;
  @Input() titleTranslation: string;
  @Input() data: EntityViewOptions[] = [];
  @Input() jsons: EntityViewOptions[];

  constructor(private activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    this.data.filter(item => item.type === 'link' && !Array.isArray(item.value)).forEach(filtered => (filtered.value = [filtered.value]));
    this.jsons = this.data.filter(obj => obj.type === 'json');
  }

  instanceOfHTMLElement(value: any): boolean {
    return value instanceof HTMLElement;
  }

  executeLink(item: EntityViewOptions, value: any): void {
    if (item.link) {
      item.link(value);
    }
  }

  close(): void {
    this.activeModal.dismiss();
  }
}
