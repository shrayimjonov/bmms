import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Subscription} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {BmFile} from '../../../shared/component/bm-file/bm-file.model';
import {Report, ReportFormat} from './report.model';
import {ReportService} from './report.service';
import {Lang} from '../../../config/language.constants';

@Component({
  templateUrl: 'report-update-modal.template.html',
})
export class ReportUpdateModalComponent implements OnInit, OnDestroy {
  private subscribers = new Subscription();

  report: Report;
  loading: boolean;
  langs = Object.keys(Lang).map(key => Lang[key]);
  formats = Object.keys(ReportFormat).map(key => ReportFormat[key]);

  editForm = this.fb.group({
    id: [],
    title: [null, [Validators.required, Validators.maxLength(128)]],
    fileName: [null, [Validators.required, Validators.maxLength(64)]],
    desc: [null, [Validators.required, Validators.maxLength(255)]],
    lang: [Lang.RU, Validators.required],
    template: [null],
    enabled: [true],
    roles: [null, Validators.maxLength(255)],
    defaultFormat: [ReportFormat.xlsx, Validators.required],
    ord: [null],
  });

  constructor(
    private readonly reportService: ReportService,
    private readonly activeModal: NgbActiveModal,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    if (this.report?.id) {
      this.updateForm();
    }
  }

  private updateForm(): void {
    this.editForm.patchValue({
      ...this.report,
      template: {
        name: 'file',
        content: this.report.template,
        contentType: this.report.templateContentType,
      } as BmFile,
    });
  }

  save(): void {
    const file: BmFile = this.editForm.get('template')?.value;

    const report: Report = this.editForm.getRawValue();
    report.template = file?.content;
    report.templateContentType = file?.contentType;

    if (this.report?.id) {
      this.update(report);
    } else {
      this.create(report);
    }
  }

  private create(report: Report): void {
    this.loading = true;
    this.subscription = this.reportService
      .create(report)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe(() => this.onSuccess());
  }

  private update(report: Report): void {
    this.loading = true;
    this.subscription = this.reportService
      .update(report)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe(() => this.onSuccess());
  }

  protected onSuccess(): void {
    this.activeModal.close();
  }

  close(): void {
    this.activeModal.dismiss();
  }

  private set subscription(subscription: Subscription) {
    this.subscribers.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscribers.unsubscribe();
  }
}
