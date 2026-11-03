import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {Bill} from '../../bill/bill.model';
import {HttpResponse} from '@angular/common/http';
import {Recipient} from '../../recipient/recipient.model';
import {filter, finalize, pluck, takeUntil} from 'rxjs/operators';
import {RecipientService} from '../../recipient/recipient.service';
import {BillService} from '../../bill/bill.service';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {ActivatedRoute} from '@angular/router';
import {AgentService} from '../../agent/agent.service';
import {Agent} from '../../agent/agent.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {BillInfoModalComponent} from '../../bill/bill-info-modal.component';

@Component({
  selector: 'transaction-bills',
  templateUrl: 'transaction-bills.template.html',
  styleUrls: ['../../bill/bill-type.style.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [BmAlertService],
  styles: [
    `
      th {
        border-top: 1px solid #f0f0f0;
      }
    `,
  ],
})
export class TransactionBillsComponent implements OnInit, OnDestroy {
  @Input() transactionId: number;

  private readonly destroy$ = new Subject<void>();

  bills: Bill[];
  loading = true;
  predicate!: string;
  ascending!: boolean;

  constructor(
    public readonly billService: BillService,
    private readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly alertService: BmAlertService,
    private readonly agentService: AgentService,
    private readonly modalService: NgbModal,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.handleNavigation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private handleNavigation(): void {
    const sort = (this.activatedRoute.snapshot.queryParamMap.get('sort') || this.activatedRoute.snapshot.data['defaultSort']).split(',');
    const predicate = sort[0];
    const ascending = sort[1] === 'asc';

    if (predicate !== this.predicate || ascending !== this.ascending) {
      this.predicate = predicate;
      this.ascending = ascending;
      this.loadBills();
    }
  }

  loadBills(): void {
    const req = {
      size: 2000,
      sort: this.sort(),
      'transactionId.equals': this.transactionId,
    };

    this.billService
      .query(req)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe((res: HttpResponse<Bill[]>) => {
        this.bills = res.body;

        this.alertService.clear();
        if (!res.body?.length) {
          this.alertService.warning('billMasterGwApp.pkbillmsBill.home.notFound');
        }
      });
  }

  private sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  viewBill(bill: Bill): void {
    const modal = this.modalService.open(BillInfoModalComponent, { modalDialogClass: 'bill-info-modal' });
    modal.componentInstance.bill = bill;
  }

  viewRecipient(id: number): void {
    this.recipientService
      .find(id)
      .pipe(
        pluck('body'),
        takeUntil(this.destroy$),
        filter(r => !!r)
      )
      .subscribe((recipient: Recipient) => this.recipientService.openView(recipient));
  }

  viewAgent(shortName: string): void {
    const req = {
      size: 1,
      'shortName.equals': shortName,
    };

    this.agentService
      .query(req)
      .pipe(
        pluck('body'),
        takeUntil(this.destroy$),
        filter(a => !!a?.length)
      )
      .subscribe((agents: Agent[]) => this.agentService.openView(agents[0]));
  }
}
