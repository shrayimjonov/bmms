import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../config/event.constants';
import {BinPcType, HumoBin} from './humo-bin.model';
import {HumoBinService} from './humo-bin.service';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'humo-bin-update-modal.template.html',
})
export class HumoBinUpdateModalComponent extends EntityUpdateBaseComponent<HumoBin, HumoBinService> implements OnInit, OnDestroy {
  readonly pcTypes = Object.keys(BinPcType);

  constructor(private readonly humoBinService: HumoBinService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, humoBinService, Event.HUMO_BIN_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      bin: [null, [Validators.required, Validators.minLength(1), Validators.maxLength(32)]],
      bankName: [null, [Validators.maxLength(128)]],
      pcType: [],
      useCredit: [false],
      useDebit: [false],
      useReverse: [false],
      useReconcile: [false],
      comment: [null, [Validators.maxLength(256)]],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
