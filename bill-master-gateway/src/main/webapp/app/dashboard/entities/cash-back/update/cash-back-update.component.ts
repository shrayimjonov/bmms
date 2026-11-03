import { Component, OnDestroy, OnInit } from '@angular/core';
import { EntityUpdateBaseComponent } from '../../../../shared/entity/entity-update-base-component';
import { ICashBack } from '../cash-back.model';
import { CashBackService } from '../service/cash-back.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Event } from '../../../../config/event.constants';
import { Validators } from '@angular/forms';
import { CashBackStatus, CashBackType } from '../../../../shared/model/enumerations/cash-back-type.model';
import { getJsonEditorOptions } from '../../../../shared/functions';

@Component({
  templateUrl: 'cash-back-update.component.html',
})
export class CashBackUpdateComponent extends EntityUpdateBaseComponent<ICashBack, CashBackService> implements OnInit, OnDestroy {
  cashBackType = Object.keys(CashBackType);
  cashbackStatus = Object.keys(CashBackStatus);
  editorOptions = getJsonEditorOptions();

  constructor(private cashbackService: CashBackService, private readonly activeModal: NgbActiveModal) {
    super(activeModal, cashbackService, Event.CASH_BACK_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      bankCommission: [null, [Validators.required]],
      clientCommission: [null, [Validators.required]],
      name: [null, Validators.maxLength(64)],
      type: [null],
      status: [null],
      jsonParams: [null],
    });

    if (this.data.id) {
      this.editForm.patchValue({
        jsonParams: JSON.parse(this.data.jsonParams),
      });
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const cashback: ICashBack = this.getRawValue();
    cashback.jsonParams = JSON.stringify(this.editForm.get('jsonParams')?.value);
    console.log(cashback);
    this.save(cashback);
  }
}
