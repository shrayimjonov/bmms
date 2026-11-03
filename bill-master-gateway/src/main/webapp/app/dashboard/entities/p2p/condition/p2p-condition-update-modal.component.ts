import {Component, OnDestroy, OnInit} from '@angular/core';
import {P2pCondition} from './p2p-condition.model';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {P2pConditionService} from './p2p-condition.service';
import {Event} from '../../../../config/event.constants';
import {EntityUpdateBaseComponent} from "../../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'p2p-condition-update-modal.template.html',
})
export class P2pConditionUpdateModalComponent extends EntityUpdateBaseComponent<P2pCondition, P2pConditionService> implements OnInit, OnDestroy {
  constructor(private readonly activeModal: NgbActiveModal,
              private readonly p2pConditionService: P2pConditionService,) {
    super(activeModal, p2pConditionService, Event.P2P_CONDITION_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [null],
      value: [null, Validators.required],
      description: [null]
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
