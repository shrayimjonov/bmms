import {AfterViewInit, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NzSiderComponent} from 'ng-zorro-antd/layout';
import {VERSION} from '../app.constants';
import {LoginService} from '../authorization/login/login.service';
import {Subject} from 'rxjs';
import {filter, map, switchMap, takeUntil} from 'rxjs/operators';
import {LanguageService} from '../core/util/language.service';
import {DASHBOARD_SIDEBAR_MENUS} from './dashboard-sidebar-menus';
import {ActivatedRoute, Data, NavigationEnd, Router} from '@angular/router';
import {AccountService} from "../core/auth/account.service";

@Component({
  templateUrl: 'dashboard.template.html',
  styleUrls: ['dashboard.style.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(NzSiderComponent) nzSider: NzSiderComponent;

  isClosed: boolean;
  contentBgColor: string;
  openSearchedMenu = false;

  readonly menus = DASHBOARD_SIDEBAR_MENUS;
  readonly version = VERSION ? (VERSION.toLowerCase().startsWith('v') ? VERSION : 'v' + VERSION) : '';

  private readonly destroy$ = new Subject<void>();

  constructor(public readonly accountService: AccountService,
              private readonly loginService: LoginService,
              private readonly langService: LanguageService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly router: Router,
              private readonly cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.loadActiveRouteData();
    this.catchRouteDataOnNavigation();
  }

  ngAfterViewInit(): void {
    // For fix (expression changed) error
    this.cdr.detectChanges();
  }

  private loadActiveRouteData(): void {
    this.getLastChild(this.activatedRoute).data
      .pipe(
        takeUntil(this.destroy$)
      )
      .subscribe((data: Data) => this.onDataReceived(data));
  }

  // noinspection JSMethodCanBeStatic
  private getLastChild(activatedRoute: ActivatedRoute): ActivatedRoute {
    let child = activatedRoute.firstChild;
    while (child?.firstChild) {
      child = child.firstChild;
    }
    return child;
  }

  private catchRouteDataOnNavigation(): void {
    this.router.events
      .pipe(
        takeUntil(this.destroy$),
        filter(event => event instanceof NavigationEnd),
        map(() => this.getLastChild(this.activatedRoute)),
        switchMap((route: ActivatedRoute) => route.data)
      )
      .subscribe((data: Data) => this.onDataReceived(data));
  }

  private onDataReceived(data: Data): void {
    this.contentBgColor = data.contentBgColor || '#fff';
  }

  @HostListener('window:resize', ['$event'])
  private onResize(event: any): void {
    this.isClosed = event.target.innerWidth <= 950;
  }

  toggle(): void {
    this.isClosed = !this.isClosed;
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  searchMenu(value: string): void {
    const ul = document.getElementById('dashboard');
    const li = ul.getElementsByTagName('li');
    for (let i = 0; i < li.length; i++) {
      if (li[i].innerText.toLocaleLowerCase().includes(value.toLocaleLowerCase())) {
        if (value !== '') {
          this.openSearchedMenu = true;
        } else if (value === '') {
          this.openSearchedMenu = false;
        }
        li[i].style.display = '';
      } else {
        li[i].style.display = 'none';
      }
    }
  }
}
