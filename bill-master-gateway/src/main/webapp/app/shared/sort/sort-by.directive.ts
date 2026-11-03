import {AfterContentInit, ContentChild, Directive, Host, HostListener, Input, OnDestroy} from '@angular/core';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {SortDirective} from './sort.directive';
import {SortDown, SortIcon, SortUp} from "../../config/nz-icons";
import {IconDefinition} from "@ant-design/icons-angular";
import {SortIconComponent} from "./sort-icon.component";

@Directive({
  selector: '[jhiSortBy]'
})
export class SortByDirective<T> implements AfterContentInit, OnDestroy {
  @Input() jhiSortBy?: T;

  @ContentChild(SortIconComponent, {static: true})
  iconComponent?: SortIconComponent;

  readonly sortIcon = SortIcon;
  readonly sortAscIcon = SortUp;
  readonly sortDescIcon = SortDown;

  private readonly destroy$ = new Subject<void>();

  constructor(@Host() private sort: SortDirective<T>) {
    sort.predicateChange.pipe(takeUntil(this.destroy$)).subscribe(() => this.updateIconDefinition());
    sort.ascendingChange.pipe(takeUntil(this.destroy$)).subscribe(() => this.updateIconDefinition());
  }

  @HostListener('click')
  onClick(): void {
    this.sort.sort(this.jhiSortBy);
  }

  ngAfterContentInit(): void {
    this.updateIconDefinition();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateIconDefinition(): void {
    if (this.iconComponent) {
      let icon: IconDefinition = this.sortIcon;
      if (this.sort.predicate === this.jhiSortBy) {
        icon = this.sort.ascending ? this.sortAscIcon : this.sortDescIcon;
      }
      this.iconComponent.icon = icon.name;
    }
  }
}
