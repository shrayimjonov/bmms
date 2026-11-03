import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Validators} from '@angular/forms';
import {UserGroup, UserGroupAuthority} from './user-group.model';
import {finalize, pluck, takeUntil} from 'rxjs/operators';
import {UserGroupService} from './user-group.service';
import {AuthorityService} from '../authority/authority.service';
import {Event} from "../../../../config/event.constants";
import {EntityUpdateBaseComponent} from "../../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'user-group-update-modal.template.html',
})
export class UserGroupUpdateModalComponent extends EntityUpdateBaseComponent<UserGroup, UserGroupService> implements OnInit, OnDestroy {
  authorities: string[];

  constructor(private readonly activeModal: NgbActiveModal,
              private readonly authorityService: AuthorityService,
              private readonly userGroupService: UserGroupService) {
    super(activeModal, userGroupService, Event.USER_GROUP_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [null],
      name: [null, [Validators.required, Validators.maxLength(64)]],
      details: [null],
      authorities: [[], Validators.required],
      active: [true],
      type: [null]
    });

    this.loadAuthorities();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadAuthorities(name?: string): void {
    const query = {
      size: 2000
    };

    if (name) {
      query['name.contains'] = name;
    }

    this.loading = true;
    this.authorityService.query(query)
      .pipe(
        pluck('body'),
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((authorities: UserGroupAuthority[]) => this.authorities = authorities?.map(authority => authority.name));
  }
}
