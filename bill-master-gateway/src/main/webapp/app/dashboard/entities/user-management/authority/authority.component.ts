import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthorityService} from './authority.service';
import {UserGroupAuthority} from '../user-group/user-group.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EntityDeleteModalComponent} from '../../../../shared/entity/entity-delete/entity-delete-modal.component';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Event} from '../../../../config/event.constants';
import {EntityBaseComponent} from "../../../../shared/entity/entity-base-component";

@Component({
  templateUrl: 'authority.template.html',
})
export class AuthorityComponent extends EntityBaseComponent<UserGroupAuthority> implements OnInit, OnDestroy {
  searchText: string;

  constructor(private readonly modalService: NgbModal,
              private readonly authorityService: AuthorityService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute, () => this.loadPage());
    this.registerToModify(Event.AUTHORITY_LIST_MODIFICATION, () => this.loadPage());
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
    this.authorityService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<UserGroupAuthority[]>) => this.onDataReceived(response));
  }

  delete(name: string): void {
    const modal = this.modalService.open(EntityDeleteModalComponent);
    modal.componentInstance.useFunction = this.authorityService.delete(name);
    modal.componentInstance.translateKey = 'authority.deleted';
    modal.componentInstance.translateValues = name;
  }
}
