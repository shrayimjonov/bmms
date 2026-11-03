import {Injectable} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EntityViewOptions} from './entity-view/entity-view-options.model';
import {EntityViewModalComponent} from './entity-view/entity-view-modal.component';
import {EntityDeleteModalComponent} from './entity-delete/entity-delete-modal.component';
import {EntityDeleteOptions} from './entity-delete/entity-delete-options.model';
import {EntityUpdateOptions} from './entity-update/entity-update-options.model';
import {EntityAuditHistoryModalComponent} from './entity-audit/entity-audit-history-modal.component';
import {EntityHistoryOptions} from './entity-audit/entity-history-options.model';
import {AccountService} from "../../core/auth/account.service";
import {Authority} from "../../config/authority.constants";
import {BmAlertService} from "../component/bm-alert/bm-alert.service";

@Injectable({providedIn: 'root'})
export class EntityService {
  constructor(private readonly modalService: NgbModal,
              private readonly accountService: AccountService,
              private readonly alertService: BmAlertService) {
  }

  update<T>(options: EntityUpdateOptions<T>): NgbModalRef {
    const modalOptions: any = {
      backdrop: 'static',
      ...options.modalOptions
    };
    const modalRef = this.modalService.open(options.component, modalOptions);
    if (options.componentInstances) {
      Object.keys(options.componentInstances).forEach(key => (modalRef.componentInstance[key] = options.componentInstances[key]));
    }

    return modalRef;
  }

  view(options: EntityViewOptions[], id: number | string, authorities?: Authority[]): NgbModalRef | null {
    if (this.accountService.hasAnyAuthority(authorities)) {
      const modalRef = this.modalService.open(EntityViewModalComponent);
      modalRef.componentInstance.id = id;
      modalRef.componentInstance.data = options;

      return modalRef;
    }

    this.alertService.warning('global.not-show-role', null, true);
    return null;
  }

  delete(options: EntityDeleteOptions): NgbModalRef {
    const modalRef = this.modalService.open(EntityDeleteModalComponent);
    modalRef.componentInstance.useFunction = options.useFunction;
    modalRef.componentInstance.event = options.event;
    modalRef.componentInstance.translateKey = options.alertTranslation;
    modalRef.componentInstance.translateValues = options.alertTranslationValue;

    return modalRef;
  }

  history(options: EntityHistoryOptions): NgbModalRef {
    const modalRef = this.modalService.open(EntityAuditHistoryModalComponent, {size: 'lg'});

    modalRef.componentInstance.qualifiedName = options.qualifiedName;
    modalRef.componentInstance.entityId = '' + options.entityId;
    modalRef.componentInstance.i18nPrefix = options.i18nPrefix;

    return modalRef;
  }
}
