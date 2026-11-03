import {Component, OnDestroy, OnInit} from '@angular/core';
import {P2pCommission} from './p2p-commission.model';
import {Validators} from '@angular/forms';
import {P2pCommissionService} from './p2p-commission.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../../config/event.constants';
import {EntityUpdateBaseComponent} from "../../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'p2p-commission-update-modal.template.html',
})
export class P2pCommissionUpdateModalComponent extends EntityUpdateBaseComponent<P2pCommission, P2pCommissionService> implements OnInit, OnDestroy {
  constructor(private readonly activeModal: NgbActiveModal,
              private readonly p2pCommissionService: P2pCommissionService) {
    super(activeModal, p2pCommissionService, Event.P2P_COMMISSION_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required]],
      up: [null, [Validators.min(0), Validators.max(100)]],
      down: [null, [Validators.min(0), Validators.max(100)]],
      fee: [null, [Validators.min(0), Validators.max(100)]],
      description: [null],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
