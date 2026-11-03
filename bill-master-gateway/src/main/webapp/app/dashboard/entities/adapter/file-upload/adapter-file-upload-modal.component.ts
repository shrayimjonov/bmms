import {Component, OnDestroy} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {AdapterService} from '../adapter.service';
import {finalize} from 'rxjs/operators';
import {Subscription} from 'rxjs/internal/Subscription';
import {BmFile} from '../../../../shared/component/bm-file/bm-file.model';
import {AdapterFileUpload} from './adapter-file-upload.model';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {Result} from '../../../../shared/model/pkbillms/result.model';

@Component({
  templateUrl: 'adapter-file-upload-modal.template.html',
  providers: [AdapterService]
})
export class AdapterFileUploadModalComponent implements OnDestroy {
  private subscription?: Subscription;

  file: BmFile;
  adapterName: string;
  loading: boolean;

  constructor(private activeModal: NgbActiveModal, private alertService: BmAlertService, private adapterService: AdapterService) {}

  save(): void {
    const file: AdapterFileUpload = {
      name: this.adapterName,
      fileContentType: this.file.contentType,
      file: this.file.content,
    };

    this.loading = true;
    this.subscription = this.adapterService
      .syncWithFile(file)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(
        (result: Result) => this.onSynced(result),
        () => this.onSyncError()
      );
  }

  close(): void {
    this.activeModal.dismiss('close');
  }

  private onSynced(result: Result): void {
    if (result.success) {
      this.close();
      this.alertService.info('global.adapter.sync.success', true);
    } else {
      this.onSyncError();
    }
  }

  private onSyncError(): void {
    this.alertService.error('global.adapter.sync.error');
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
