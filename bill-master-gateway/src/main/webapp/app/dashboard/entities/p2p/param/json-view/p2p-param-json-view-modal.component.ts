import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  templateUrl: 'p2p-param-json-view-modal.template.html'
})
export class P2pParamJsonViewModalComponent {
  data: any;

  constructor(public readonly activeModal: NgbActiveModal) {}
}
