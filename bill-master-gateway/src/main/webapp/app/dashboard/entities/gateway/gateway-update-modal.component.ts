import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {takeUntil} from 'rxjs/operators';
import {Gateway} from './gateway.model';
import {GatewayService} from './gateway.service';
import {Event} from '../../../config/event.constants';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";
import {Validators} from "@angular/forms";

@Component({
  templateUrl: 'gateway-update-modal.template.html',
})
export class GatewayUpdateModalComponent extends EntityUpdateBaseComponent<Gateway, GatewayService> implements OnInit, OnDestroy {
  springBeanNames: string[];

  constructor(private readonly activeModal: NgbActiveModal,
              private readonly gatewayService: GatewayService) {
    super(activeModal, gatewayService, Event.GATEWAY_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(64)]],
      org: [null, [Validators.required, Validators.maxLength(32)]],
      mfo: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(5)]],
      ban: [null, [Validators.required, Validators.maxLength(32)]],
      taxnr: [null, [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
      contract: [null, [Validators.required, Validators.maxLength(64)]],
      springBeanName: [null, Validators.required],
      active: [true],
    });

    this.loadSpringBeanNames();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private loadSpringBeanNames(): void {
    this.gatewayService.getSpringBeanNames()
      .pipe(
        takeUntil(this.destroy$),
      )
      .subscribe((springBeanNames: string[]) => (this.springBeanNames = springBeanNames));
  }
}
