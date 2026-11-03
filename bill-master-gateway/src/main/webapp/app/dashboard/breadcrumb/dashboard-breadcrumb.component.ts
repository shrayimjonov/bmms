import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {DashboardBreadcrumb} from './dashboard-breadcrumb.model';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {filter, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'dashboard-breadcrumb',
  templateUrl: 'dashboard-breadcrumb.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardBreadcrumbComponent implements OnInit, OnDestroy {
  breadcrumbs: DashboardBreadcrumb[] = [];

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly activatedRoute: ActivatedRoute, private readonly router: Router, private readonly cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchBreadcrumbs(this.activatedRoute, undefined, true);
    this.registerOnChangeNavigation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private fetchBreadcrumbs(route: ActivatedRoute, url = '.', force?: boolean): void {
    if (force) {
      this.breadcrumbs = [];
    }

    const children = route.children;

    for (const child of children) {
      const routeURL = child.snapshot.url.map(segment => segment.path).join('/');
      const title = child.snapshot.data.pageTitle || routeURL;

      if (routeURL && !child.snapshot.data.noBreadcrumb) {
        url += `/${routeURL}`;
        this.breadcrumbs.push({ title, url });
      }

      this.fetchBreadcrumbs(child, url);
    }

    this.cdr.markForCheck();
  }

  private registerOnChangeNavigation(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.fetchBreadcrumbs(this.activatedRoute, undefined, true));
  }
}
