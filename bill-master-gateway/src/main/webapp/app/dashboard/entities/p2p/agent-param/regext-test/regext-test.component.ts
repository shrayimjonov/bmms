import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Util } from '../../../../../shared/util';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  template: `
    <form [formGroup]="regexForm" *ngIf="regexForm">
      <div class="modal-body">
        <label for="test">Pattern test input</label>
        <input id="test" nz-input formControlName="test" input-validator />
      </div>

      <div class="modal-footer">
        <button type="button" nz-button nzSize="large" nzType="primary" jhiTranslate="entity.action.close" (click)="closeModal()">
          Close
        </button>
      </div>
    </form>
  `,
})
export class RegexTestComponent implements OnInit {
  regex = '';

  private readonly fb = Util.inject(FormBuilder);

  regexForm: FormGroup;

  constructor(private readonly activeModal: NgbActiveModal) {}

  ngOnInit() {
    this.regexForm = this.fb.group({
      test: [null, [Validators.pattern(this.regex)]],
    });
  }

  closeModal() {
    this.activeModal.close();
  }
}
