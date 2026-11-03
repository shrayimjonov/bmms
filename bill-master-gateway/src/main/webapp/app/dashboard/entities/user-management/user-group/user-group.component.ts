import {Component, OnDestroy, OnInit} from '@angular/core';
import {UserGroup} from './user-group.model';
import {UserGroupService} from './user-group.service';
import {Event} from '../../../../config/event.constants';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';

@Component({
  templateUrl: 'user-group.template.html',
})
export class UserGroupComponent extends EntityBaseComponent<UserGroup> implements OnInit, OnDestroy {
  searchText: string;

  constructor(
    public readonly userGroupService: UserGroupService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.userGroupService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.USER_GROUP_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = this.getRequestParams(page);

    if (this.searchText) {
      req['search'] = this.searchText;
    }

    this.loading = true;
    this.userGroupService
      .query(req)
      .pipe(this.loadPageOperators())
      .subscribe((response: HttpResponse<UserGroup[]>) => this.onDataReceived(response));
  }

  isNotRemovable(group: UserGroup): boolean {
    return !!group.type;
  }
}
