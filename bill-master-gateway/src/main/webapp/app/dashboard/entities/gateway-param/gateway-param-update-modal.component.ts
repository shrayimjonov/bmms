import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {GatewayParam} from './gateway-param.model';
import {GatewayParamService} from './gateway-param.service';
import {RequestType} from '../../../shared/model/enumerations/request-type.model';
import {MethodType} from '../../../shared/model/enumerations/method-type.model';
import {ProtocolType} from '../../../shared/model/enumerations/protocol-type.model';
import {ModeType} from '../../../shared/model/enumerations/mode-type.model';
import {RecipientService} from '../recipient/recipient.service';
import {Event} from '../../../config/event.constants';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'gateway-param-update-modal.template.html',
})
export class GatewayParamUpdateModalComponent extends EntityUpdateBaseComponent<GatewayParam, GatewayParamService> implements OnInit, OnDestroy {
  readonly requestTypes = Object.keys(RequestType);
  readonly httpMethodType = Object.keys(MethodType);
  readonly mode = Object.keys(ModeType);
  readonly protocol = Object.keys(ProtocolType);

  constructor(public readonly recipientService: RecipientService,
              private readonly activeModal: NgbActiveModal,
              private readonly gatewayParamService: GatewayParamService) {
    super(activeModal, gatewayParamService, Event.GATEWAY_PARAM_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      url: [null, [Validators.required, Validators.maxLength(512)]],
      type: [null, Validators.required],
      username: [null, [Validators.required, Validators.maxLength(32)]],
      password: [null, [Validators.required, Validators.maxLength(64)]],
      token: [null, Validators.maxLength(128)],
      body: [null, Validators.maxLength(2048)],
      statusQuery: [null, Validators.maxLength(256)],
      messageQuery: [null, Validators.maxLength(256)],
      successCode: [null, Validators.maxLength(32)],
      callback: [null, Validators.maxLength(512)],
      timeout: [null],
      headers: [null, Validators.maxLength(1024)],
      dataQueries: [null, Validators.maxLength(1024)],
      httpMethodType: [null, Validators.required],
      protocol: [null, Validators.required],
      requestMediaType: [null, Validators.maxLength(64)],
      responseMediaType: [null, Validators.maxLength(64)],
      active: [true],
      mode: [null, Validators.required],
      extIdQuery: [null],
      successOn2xx: [true],
      recipient: [null],
    });

    if (this.data.id) {
      this.editForm.patchValue({
        headers: JSON.parse(this.data.headers),
        dataQueries: JSON.parse(this.data.dataQueries)
      }, {emitEvent: false});
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const headers = this.editForm.get('headers')?.value;
    const dataQueries = this.editForm.get('dataQueries')?.value;

    const gatewayParam: GatewayParam = this.getRawValue();
    gatewayParam.headers = headers ? JSON.stringify(headers) : null;
    gatewayParam.dataQueries = dataQueries ? JSON.stringify(dataQueries) : null;
    gatewayParam.recipient = getRelation(this.editForm.get('recipient').value);

    this.save(gatewayParam);
  }
}
