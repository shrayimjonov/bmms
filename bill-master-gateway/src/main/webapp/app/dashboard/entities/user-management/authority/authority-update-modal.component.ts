import {Component, OnDestroy, OnInit} from '@angular/core';

import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {UserGroupAuthority} from '../user-group/user-group.model';
import {AuthorityService} from './authority.service';
import {Event} from "../../../../config/event.constants";
import {EntityUpdateBaseComponent} from "../../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'authority-update-modal.template.html',
})
export class AuthorityUpdateModalComponent extends EntityUpdateBaseComponent<UserGroupAuthority, AuthorityService> implements OnInit, OnDestroy {
  constructor(private readonly activeModal: NgbActiveModal,
              private readonly authorityService: AuthorityService) {
    super(activeModal, authorityService, Event.AUTHORITY_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(50)]],
      details: [null]
    });

    if (this.data && this.data['name']) {
      this.editForm.patchValue(this.data, {emitEvent: false});
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
