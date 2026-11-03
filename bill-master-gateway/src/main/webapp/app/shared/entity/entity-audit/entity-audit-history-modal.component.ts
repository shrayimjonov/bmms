import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {EntityAuditService} from './entity-audit.service';
import * as moment from 'moment';
import {TranslateService} from '@ngx-translate/core';
import {finalize, takeUntil} from 'rxjs/operators';
import {Subject} from "rxjs";

@Component({
  templateUrl: 'entity-audit-history-modal.component.html',
  styleUrls: ['entity-audit-history.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityAuditHistoryModalComponent implements OnInit, OnDestroy {
  commits: any;
  qualifiedName: string;
  entityId: string;
  i18nPrefix: string;
  loading: boolean;

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly service: EntityAuditService,
              private readonly activeModal: NgbActiveModal,
              private readonly cdr: ChangeDetectorRef,
              private readonly langService: TranslateService) {
  }

  ngOnInit(): void {
    moment.locale(this.langService.currentLang);
    this.loadHistory();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadHistory(): void {
    this.loading = true;
    this.service
      .getHistoryByEntityId(this.qualifiedName, this.entityId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe((commits: any[]) => {
        this.modifyChangedFields(commits);
        this.commits = commits;
      });
  }

  close(): void {
    this.activeModal.dismiss('close');
  }

  getJsonString(data: any): string {
    if (data) {
      if (data !== '""') {
        return JSON.stringify(data);
      } else {
        return data;
      }
    }
    return '';
  }

  // noinspection JSMethodCanBeStatic
  private modifyChangedFields(commits: any): void {
    for (let i = 0; i < commits.length; i++) {
      const commit = commits[i];

      commit.duration = moment(commit.commitMetadata.commitDate).fromNow();

      commit.diff = {};

      if (commits[i + 1]) {
        const prev = commits[i + 1];

        for (const changedPropName of commit.changedProperties) {
          // commit.diff['prop'] = changedPropName;
          commit.diff[changedPropName] = {};
          commit.diff[changedPropName]['left'] = prev.state[changedPropName];
          commit.diff[changedPropName]['right'] = commit.state[changedPropName];
        }
      } else {
        for (const changedPropName of commit.changedProperties) {
          // commit.diff['prop'] = changedPropName;
          commit.diff[changedPropName] = {};
          commit.diff[changedPropName]['left'] = '""';
          commit.diff[changedPropName]['right'] = commit.state[changedPropName];
        }
      }
    }
  }
}
