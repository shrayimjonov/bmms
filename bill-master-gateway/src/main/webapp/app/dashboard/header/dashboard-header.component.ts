import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import {NzSiderComponent} from 'ng-zorro-antd/layout';
import {Authority} from '../../config/authority.constants';
import {LanguageService} from '../../core/util/language.service';
import {LoginService} from '../../authorization/login/login.service';
import {LANGUAGES} from '../../config/language.constants';
import {takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'dashboard-header',
  templateUrl: 'dashboard-header.template.html',
  styleUrls: ['dashboard-header.style.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardHeaderComponent implements AfterViewInit, OnDestroy {
  @Input() nzSider: NzSiderComponent;
  @Output() toggleSider = new EventEmitter<void>();

  private readonly destroy$ = new Subject<void>();

  activeLanguage = this.langService.getCurrent();
  readonly authority = Authority;
  readonly languages = LANGUAGES;

  constructor(
    private readonly langService: LanguageService,
    private readonly cdr: ChangeDetectorRef,
    private readonly loginService: LoginService
  ) {}

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeLanguage(languageKey: string): void {
    this.langService
      .change(languageKey)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => (this.activeLanguage = languageKey));
  }

  logout(): void {
    this.loginService.logout().subscribe();
  }
}
