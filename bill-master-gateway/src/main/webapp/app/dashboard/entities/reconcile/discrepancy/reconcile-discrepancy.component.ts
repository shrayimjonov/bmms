import {Component, OnDestroy, OnInit} from '@angular/core';
import {ReconcileDiscrepancyService} from './reconcile-discrepancy.service';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {ReconcileDiscrepancy} from './reconcile-discrepancy.model';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'reconcile-discrepancy.template.html',
})
export class ReconcileDiscrepancyComponent extends EntityBaseComponent<ReconcileDiscrepancy> implements OnInit, OnDestroy {
  private reconcileSummaryId: number;

  constructor(
    private readonly reconcileDiscrepancyService: ReconcileDiscrepancyService,
    protected readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.reconcileSummaryId = this.activatedRoute.snapshot.queryParams.reconcileSummaryId;
    this.handleNavigation(this.activatedRoute, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = this.getRequestParams(page);

    if (this.reconcileSummaryId) {
      req['reconcileSummaryId.equals'] = this.reconcileSummaryId;
    }

    this.loading = true;
    this.reconcileDiscrepancyService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<ReconcileDiscrepancy[]>) => this.onDataReceived(response));
  }
}
