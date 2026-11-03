import {ChangeDetectionStrategy, Component, OnDestroy} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {AgentRecipientSettingService} from '../../agent-recipient-setting.service';
import {AgentRecipientSettingResolver} from '../../agent-recipient-setting-resolver.model';
import {finalize, takeUntil} from 'rxjs/operators';
import {PcSetting} from '../../pc-setting.model';
import {Subject} from "rxjs";

@Component({
  templateUrl: 'epos-expression-checker-modal.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EposExpressionCheckerModalComponent implements OnDestroy {
  request: AgentRecipientSettingResolver;
  response: PcSetting;
  loading: boolean;

  private readonly destroy$ = new Subject<void>();

  constructor(public readonly activeModal: NgbActiveModal,
              private readonly agentRecipientSettingService: AgentRecipientSettingService) {
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  resolve(): void {
    this.loading = true;
    this.agentRecipientSettingService
      .resolve(this.request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((result: PcSetting) => (this.response = result));
  }
}
