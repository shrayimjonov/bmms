import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {pluck, takeUntil} from 'rxjs/operators';
import {Bill} from './bill.model';
import {BillService} from './bill.service';
import {RecipientService} from '../recipient/recipient.service';
import {AgentService} from '../agent/agent.service';
import {Recipient} from '../recipient/recipient.model';
import {Agent} from '../agent/agent.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {BillInfoModalComponent} from './bill-info-modal.component';
import {EntityBaseComponent} from "../../../shared/entity/entity-base-component";
import {BmAlertService} from "../../../shared/component/bm-alert/bm-alert.service";

@Component({
  templateUrl: 'bill.template.html',
  styleUrls: ['bill-type.style.scss']
})
export class BillComponent extends EntityBaseComponent<Bill> implements OnInit, OnDestroy {
  constructor(public readonly billService: BillService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly recipientService: RecipientService,
              private readonly agentService: AgentService,
              private readonly modalService: NgbModal,
              private readonly alertService: BmAlertService) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.billService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.billService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Bill[]>) => this.onDataReceived(response));
  }

  view(bill: Bill): void {
    const modal = this.modalService.open(BillInfoModalComponent, {modalDialogClass: 'bill-info-modal'});
    modal.componentInstance.bill = bill;
  }

  viewRecipient(id: number): void {
    if (id === 0 || null) this.alertService.warning('This recipient is not available in the internal system')
    this.recipientService.find(id)
      .pipe(
        takeUntil(this.destroy$),
        pluck('body')
      )
      .subscribe((recipient: Recipient) => this.recipientService.openView(recipient));
  }

  viewAgent(login: string): void {
    const req = {
      size: 1,
      'shortName.equals': login,
    };

    this.agentService.query(req)
      .pipe(
        takeUntil(this.destroy$),
        pluck('body')
      )
      .subscribe((agents: Agent[]) => this.agentService.openView(agents[0]));
  }
}
