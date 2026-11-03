import {Component, OnDestroy, OnInit} from '@angular/core';
import {P2pParam} from './p2p-param.model';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {P2pParamService} from './p2p-param.service';
import {finalize, takeUntil} from 'rxjs/operators';
import {P2pConditionService} from '../condition/p2p-condition.service';
import {P2pCommissionService} from '../commission/p2p-commission.service';
import {Event} from '../../../../config/event.constants';
import {getJsonEditorOptions, getRelation} from '../../../../shared/functions';
import {P2pProcessorType} from '../../../../shared/model/enumerations/p2p-processor-type.model';
import {EnumerationService} from '../../../../shared/service/enumeration/enumeration.service';
import {EntityUpdateBaseComponent} from '../../../../shared/entity/entity-update-base-component';

@Component({
  templateUrl: 'p2p-param-update-modal.template.html',
})
export class P2pParamUpdateModalComponent extends EntityUpdateBaseComponent<P2pParam, P2pParamService> implements OnInit, OnDestroy {
  types: string[];
  readonly editorOptions = getJsonEditorOptions();

  constructor(
    public readonly p2pConditionService: P2pConditionService,
    public readonly p2pCommissionService: P2pCommissionService,
    private readonly activeModal: NgbActiveModal,
    private readonly enumService: EnumerationService,
    private readonly p2pParamService: P2pParamService
  ) {
    super(activeModal, p2pParamService, Event.P2P_PARAM_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      priority: [null, [Validators.required]],
      active: [true, [Validators.required]],
      senderPc: [null, [Validators.required]],
      recipientPc: [null, [Validators.required]],
      value: [{}],
      senderCondition: [null, Validators.required],
      recipientCondition: [null, Validators.required],
      senderPercent: [null, Validators.required],
      recipientPercent: [null, Validators.required],
      name: [null],
      promo: [null],
    });

    if (this.data.value) {
      this.editForm.patchValue({ value: JSON.parse(this.data.value) }, { emitEvent: false });
    }

    this.loadPayTypes();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const p2pParam: P2pParam = this.getRawValue();
    p2pParam.value = JSON.stringify(this.editForm.get('value').value, null, 2);
    p2pParam.senderCondition = getRelation(this.editForm.get('senderCondition').value);
    p2pParam.recipientCondition = getRelation(this.editForm.get('recipientCondition').value);
    p2pParam.senderPercent = getRelation(this.editForm.get('senderPercent').value);
    p2pParam.recipientPercent = getRelation(this.editForm.get('recipientPercent').value);

    this.save(p2pParam);
  }

  private loadPayTypes(): void {
    this.enumService
      .getOnlyValues('P2pProcessorType', P2pProcessorType)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((payTypes: string[]) => (this.types = payTypes));
  }
}
