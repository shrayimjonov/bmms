import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {P2pParamService} from '../p2p-param.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {finalize, takeUntil} from 'rxjs/operators';
import {EnumerationService} from '../../../../../shared/service/enumeration/enumeration.service';
import {P2pProcessorType} from '../../../../../shared/model/enumerations/p2p-processor-type.model';
import {Subject} from "rxjs";

@Component({
  templateUrl: 'p2p-param-test-modal.template.html',
})
export class P2pParamTestModalComponent implements OnInit, OnDestroy {
  payTypes: string[];
  result: any;
  loading: boolean;
  promo: string;

  readonly testForm = this.fb.group({
    promo: [null],
    senderPc: [null, Validators.required],
    senderAccount: [null, Validators.required],
    recipientPc: [null, Validators.required],
    recipientAccount: [null, Validators.required],
  });

  private readonly destroy$ = new Subject<void>();

  constructor(
    public readonly activeModal: NgbActiveModal,
    private readonly p2pParamService: P2pParamService,
    private readonly enumService: EnumerationService,
    private readonly fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.loadPayTypes();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadPayTypes(): void {
    this.enumService
      .getOnlyValues('P2pProcessorType', P2pProcessorType)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((payTypes: string[]) => (this.payTypes = payTypes));
  }

  test(): void {
    const request = this.testForm.getRawValue();
    this.loading = true;
    this.p2pParamService.test(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe(
        res => (this.result = res),
        () => (this.result = null)
      );
  }
}
