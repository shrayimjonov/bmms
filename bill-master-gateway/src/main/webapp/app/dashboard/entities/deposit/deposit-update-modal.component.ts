import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Deposit} from './deposit.model';
import {DepositService} from './deposit.service';
import {GatewayService} from '../gateway/gateway.service';
import {AgentService} from '../agent/agent.service';
import {StateEnum} from '../user-management/user/user.model';
import {Event} from '../../../config/event.constants';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'deposit-update-modal.template.html',
})
export class DepositUpdateModalComponent extends EntityUpdateBaseComponent<Deposit, DepositService> implements OnInit, OnDestroy {
  constructor(public readonly gatewayService: GatewayService,
              public readonly agentService: AgentService,
              private readonly activeModal: NgbActiveModal,
              private readonly depositService: DepositService) {
    super(activeModal, depositService, Event.DEPOSIT_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      overdraftLimit: [null, [Validators.required]],
      balance: [null, [Validators.required]],
      balanceCcy: [null, [Validators.required]],
      bookedBalance: [null, [Validators.required]],
      limitInform: [null, [Validators.required]],
      limitDisable: [null, [Validators.required]],
      contactGroup: [null, [Validators.maxLength(64)]],
      blocked: [true],
      unlimit: [true],
      gateway: [null],
      agent: [null],
      virtualAccountId: [],
      state: [StateEnum.NEW],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const deposit: Deposit = this.getRawValue();
    deposit.gateway = getRelation(this.editForm.get('gateway').value);
    deposit.agent = getRelation(this.editForm.get('agent').value);
    this.save(deposit)
  }
}
