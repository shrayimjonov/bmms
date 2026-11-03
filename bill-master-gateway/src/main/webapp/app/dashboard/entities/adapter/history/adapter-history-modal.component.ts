import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {AdapterHistory} from "./adapter-history.model";
import {Util} from "../../../../shared/util";

@Component({
  templateUrl: 'adapter-history-modal.template.html'
})
export class AdapterHistoryModalComponent implements OnInit {
  history: AdapterHistory;
  adapterName: string;
  details = {};

  constructor(private activeModal: NgbActiveModal) {
  }

  ngOnInit(): void {
    if (Util.isParsable(this.history.details)) {
      this.details = JSON.parse(this.history.details);
    }
  }

  close(): void {
    this.activeModal.dismiss('close');
  }
}
