import {Component, OnDestroy, OnInit} from '@angular/core';
import {AdapterService} from './adapter.service';
import {Subscription} from 'rxjs/internal/Subscription';
import {Adapter} from './adapter.model';
import {finalize} from 'rxjs/operators';
import {Result} from '../../../shared/model/pkbillms/result.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {AdapterHistoryModalComponent} from './history/adapter-history-modal.component';
import {AdapterFileUploadModalComponent} from './file-upload/adapter-file-upload-modal.component';
import {AdapterHistory} from './history/adapter-history.model';
import {BmAlertService} from '../../../shared/component/bm-alert/bm-alert.service';
import {Authority} from "../../../config/authority.constants";

@Component({
  templateUrl: 'adapter.template.html'
})
export class AdapterComponent implements OnInit, OnDestroy {
  private subscribers = new Subscription();

  adapters: Adapter[];
  loading: boolean;
  searchText: string;

  constructor(private readonly adapterService: AdapterService,
              private readonly modalService: NgbModal,
              private readonly alertService: BmAlertService) {
  }

  ngOnInit(): void {
    this.loadAdapters();
  }

  private loadAdapters(): void {
    this.loading = true;
    this.subscription = this.adapterService
      .getListOfAdaptersV3()
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe((adapters: Adapter[]) => (this.adapters = adapters));
  }

  sync(adapter: Adapter): void {
    adapter.loading = true;
    this.subscription = this.adapterService
      .sync(adapter.name)
      .pipe(finalize(() => adapter.loading = false))
      .subscribe(
        (result: Result) => this.onSynced(result),
        () => this.onSyncError()
      );
  }

  uploadFile(adapterName: string): void {
    this.modalService.open(AdapterFileUploadModalComponent).componentInstance.adapterName = adapterName;
  }

  viewHistory(adapterName: string): void {
    this.subscription = this.adapterService.findHistoryByName(adapterName).subscribe(
      (history: AdapterHistory) => {
        if (history) {
          const modal = this.modalService.open(AdapterHistoryModalComponent);
          modal.componentInstance.adapterName = adapterName;
          modal.componentInstance.history = history;
        } else {
          this.onHistoryNotFound();
        }
      },
      () => this.onHistoryNotFound()
    );
  }

  private onSynced(result: Result): void {
    if (result.success) {
      this.alertService.success('global.adapter.sync.success');
    } else {
      this.onSyncError();
    }
  }

  private onSyncError(): void {
    this.alertService.error('global.adapter.sync.error');
  }

  private onHistoryNotFound(): void {
    this.alertService.error('global.adapter.historyNotFound');
  }

  get authority(): typeof Authority {
    return Authority;
  }

  private set subscription(subscription: Subscription) {
    this.subscribers.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscribers.unsubscribe();
  }
}
