import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {AccountService} from '../../../../core/auth/account.service';
import {ActivatedRoute} from '@angular/router';
import {pluck, takeUntil} from 'rxjs/operators';
import {StateEnum, User} from './user.model';
import {UserService} from './user.service';
import {Event} from '../../../../config/event.constants';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'user.template.html',
})
export class UserComponent extends EntityBaseComponent<User> implements OnInit, OnDestroy {
  login: string;
  readonly exclude = [
    'userGroups',
    'firstName',
    'lastName',
    'langKey',
    'createdBy',
    'lastModifiedBy',
    'lastModifiedDate',
    'authorities',
    'imageUrl',
  ];

  constructor(
    public readonly userService: UserService,
    private readonly accountService: AccountService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.userService.getFilterFields();
    this.fetchAccountLogin();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.USER_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private fetchAccountLogin(): void {
    this.accountService
      .identity()
      .pipe(pluck('login'), takeUntil(this.destroy$))
      .subscribe(login => (this.login = login));
  }

  setActive(user: User, isActivated: boolean): void {
    user.enabled = isActivated;
    this.userService.update(user).subscribe(() => this.loadPage());
  }

  loadPage(page?: number): void {
    const req = {...this.getRequestParams(page), 'state.notEquals': StateEnum.DELETED};

    this.loading = true;
    this.userService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<User[]>) => this.onDataReceived(response));
  }
}
