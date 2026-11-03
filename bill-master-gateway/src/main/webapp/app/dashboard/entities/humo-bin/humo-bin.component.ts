import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {Event} from '../../../config/event.constants';
import {HumoBin} from './humo-bin.model';
import {HumoBinService} from './humo-bin.service';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'humo-bin.template.html',
})
export class HumoBinComponent extends EntityBaseComponent<HumoBin> implements OnInit, OnDestroy {
  constructor(
    public readonly humoBinService: HumoBinService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.humoBinService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.HUMO_BIN_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.humoBinService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<HumoBin[]>) => this.onDataReceived(response));
  }
}
