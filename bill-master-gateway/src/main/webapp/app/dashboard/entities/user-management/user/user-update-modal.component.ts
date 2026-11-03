import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Validators} from '@angular/forms';
import {finalize, pluck, takeUntil} from 'rxjs/operators';
import {User} from './user.model';
import {UserService} from './user.service';
import {Event} from '../../../../config/event.constants';
import {UserGroup} from '../user-group/user-group.model';
import {UserGroupService} from '../user-group/user-group.service';
import {LOGIN_REGEX_VALIDATOR} from '../../../../config/input-validator.constants';
import {EntityUpdateBaseComponent} from '../../../../shared/entity/entity-update-base-component';

@Component({
  templateUrl: 'user-update-modal.template.html',
})
export class UserUpdateModalComponent extends EntityUpdateBaseComponent<User, UserService> implements OnInit, OnDestroy {
  userGroups: UserGroup[];

  constructor(
    private readonly activeModal: NgbActiveModal,
    private readonly userService: UserService,
    private readonly userGroupService: UserGroupService
  ) {
    super(activeModal, userService, Event.USER_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      username: [null, [Validators.required, Validators.minLength(1), Validators.maxLength(50), Validators.pattern(LOGIN_REGEX_VALIDATOR)]],
      firstName: [null, [Validators.maxLength(50)]],
      lastName: [null, [Validators.maxLength(50)]],
      email: [null, [Validators.minLength(5), Validators.maxLength(254), Validators.email]],
      password: ['12345', [Validators.maxLength(60)]],
      enabled: [true],
      temporary: [false],
      groups: [null, [Validators.required]],
    });

    this.loadUserGroups();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const user: User = this.getRawValue();
    user.userGroups = this.editForm.get('groups').value?.map(name => ({ name: name }));

    this.save(user);
  }

  isUpdate(): boolean {
    return !!this.editForm.get('id')?.value;
  }

  loadUserGroups(name?: string): void {
    const query = {
      size: 20,
    };

    if (name) {
      query['name.contains'] = name;
    }

    this.loading = true;
    this.userGroupService
      .query(query)
      .pipe(
        pluck('body'),
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((userGroups: UserGroup[]) => (this.userGroups = userGroups));
  }
}
