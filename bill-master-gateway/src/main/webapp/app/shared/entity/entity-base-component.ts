import {Authority} from '../../config/authority.constants';
import {combineLatest, Observable, Subject} from 'rxjs';
import {ASC, ITEMS_PER_PAGE, SORT} from '../../config/pagination.constants';
import {Subscription} from 'rxjs/internal/Subscription';
import {NglFilterField} from 'ngl-filter-field';
import {ActivatedRoute, Data, Params, Router} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {BmAlertService} from '../component/bm-alert/bm-alert.service';
import {finalize, takeUntil} from 'rxjs/operators';
import {Util} from '../util';
import {EventManager} from '../../core/util/event-manager.service';
import {Event} from '../../config/event.constants';

export abstract class EntityBaseComponent<ENTITY> {
  data: ENTITY[];

  protected readonly destroy$ = new Subject<void>();
  readonly authority = Authority;

  filterFields: NglFilterField[] = [];
  loading: boolean;
  page: number;
  totalItems = 0;
  predicate: string;
  ascending: boolean;
  showFilter: boolean;
  ngbPaginationPage = 1;
  itemsPerPage = ITEMS_PER_PAGE;
  queryParams: any;

  protected query: any;
  protected modifySubscription?: Subscription;
  private readonly _router = Util.inject(Router);
  private readonly _alertService = Util.inject(BmAlertService);

  trackId(index: number, item: ENTITY): number | undefined {
    return item['id'];
  }

  protected handleNavigation(activatedRoute: ActivatedRoute, cb?: (data: Data, params: Params) => void): void {
    combineLatest(activatedRoute.data, activatedRoute.queryParams, (data: Data, params: Params) => {
      this.queryParams = params;
      const pageNumber = params.page ? +params.page : 1;
      const sort = (params[SORT] ?? data['defaultSort']).split(',');
      const ascending = sort[1] === ASC;
      this.showFilter = !!this.hasQuery();
      if (pageNumber !== this.page || sort[0] !== this.predicate || ascending !== this.ascending) {
        this.predicate = sort[0];
        this.ascending = ascending;
        this.page = pageNumber;
      }

      if (cb) {
        cb(data, params);
      }
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe();
  }

  setQuery(query: any): void {
    this.query = query;
    if (!Util.isEmpty(query)) {
      this.page = 1;
    }
  }

  protected unsubscribe(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.modifySubscription?.unsubscribe();
  }

  get getOption(): any {
    return {
      totalItems: this.totalItems,
      ...this.query,
    };
  }

  protected sort(): any {
    if (this.predicate !== undefined) {
      const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
      if (this.predicate !== 'id') {
        result.push('id');
      }
      return result;
    }
  }

  protected getRequestParams(page: number): any {
    this.page = page ?? this.page ?? 1;

    return {
      ...this.queryParams,
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sort(),
      ...this.query,
    };
  }

  protected loadPageOperators<T>(): (source$: Observable<T>) => Observable<T> {
    return source$ =>
      source$.pipe(
        finalize(() => (this.loading = false)),
        takeUntil(this.destroy$)
      );
  }

  protected onDataReceived(response: HttpResponse<ENTITY[]>): void {
    this.totalItems = Number(response.headers.get('X-Total-Count'));

    this.refreshUrlParams();

    this.data = response.body ?? [];
    this.ngbPaginationPage = this.page;

    this._alertService.clear();
    if (!this.data?.length) {
      this._alertService.warning('global.data.notFound');
    }
  }

  private refreshUrlParams(): void {
    this._router
      .navigate([Util.getRouteUrlWithoutParams(this._router)], {
        queryParams: {
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      })
      .then();
  }

  private hasQuery(): boolean {
    return !!localStorage.getItem(`${Util.getRouteUrlWithoutParams(this._router)}.query`);
  }

  protected registerToModify(event: Event, cb: () => void): void {
    this.modifySubscription = Util.inject(EventManager).subscribe(event, () => cb());
  }

  abstract loadPage(page?: number): void;
}
