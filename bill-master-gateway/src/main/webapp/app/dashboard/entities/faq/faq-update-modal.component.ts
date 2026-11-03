import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';

import {Faq} from './faq.model';
import {FaqService} from './faq.service';
import {FaqCategory} from './category/faq-category.model';
import {EntityUpdateBaseComponent} from '../../../shared/entity/entity-update-base-component';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../config/event.constants';
import {FaqCategoryService} from './category/faq-category.service';
import {HttpResponse} from '@angular/common/http';
import {I18n} from '../../../shared/model/pkbillms/i18n.model';
import {BmI18nService} from '../../../shared/component/bm-i18n-input/bm-i18n.service';
import {Editor, Toolbar} from 'ngx-editor';
import {Lang} from '../../../config/language.constants';
import {takeUntil} from 'rxjs/operators';

@Component({
  templateUrl: 'faq-update-modal.template.html',
  styleUrls: ['faq.style.scss'],
})
export class FaqUpdateModalComponent extends EntityUpdateBaseComponent<Faq, FaqService> implements OnInit, OnDestroy {
  faqCategories?: FaqCategory[];
  languages = Object.keys(Lang);
  html: any;
  focus: boolean;

  toolbar: Toolbar = [
    ['bold', 'italic'],
    ['underline', 'strike'],
    ['code', 'blockquote'],
    ['ordered_list', 'bullet_list'],
    [{ heading: ['h1', 'h2', 'h3', 'h4', 'h5', 'h6'] }],
    ['link', 'image'],
    ['text_color', 'background_color'],
    ['align_left', 'align_center', 'align_right', 'align_justify'],
  ];

  constructor(
    public readonly faqService: FaqService,
    public readonly faqCategoryService: FaqCategoryService,
    private readonly activeModal: NgbActiveModal,
    private readonly i18nService: BmI18nService,
    private readonly fb: FormBuilder
  ) {
    super(activeModal, faqService, Event.FAQ_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      videoUrl: [null],
      questions: [null, Validators.required],
      faqCategory: [null],
      active: [true],
      answers: this.fb.array([]),
    });

    this.initI18nsWithEditor();

    this.loadFaqCategory();

    if (this.data?.id) {
      this.updateForm();
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  updateForm(): void {
    const answerGroups: FormArray = this.editForm.get('answers') as FormArray;
    answerGroups.controls.forEach(answerGroup => {
      const selectedFaq = this.data.answers?.find(answer => answer.lang === answerGroup.get('lang')?.value);
      answerGroup.get('text')?.setValue(JSON.parse(selectedFaq.text));
    });
  }

  onSubmit(): void {
    const rawValue = this.getRawValue();
    rawValue.answers.forEach(answer => {
      answer.text = JSON.stringify(answer.text);
      delete answer['editor'];
    });
    this.save(rawValue);
  }

  private initI18nsWithEditor(): void {
    Object.keys(Lang).forEach(lang => {
      (this.editForm.get('answers') as FormArray).push(
        this.fb.group({
          id: [null],
          lang: [lang],
          text: [null],
          editor: new Editor(),
        })
      );
    });
  }

  typeAsGroup(group: any): FormGroup {
    return group as FormGroup;
  }

  typeAsControl(control: any): FormControl {
    return control as FormControl;
  }

  loadFaqCategory(): void {
    this.faqCategoryService
      .query()
      .pipe(takeUntil(this.destroy$))
      .subscribe((res: HttpResponse<FaqCategory[]>) => (this.faqCategories = res.body));
  }

  getI18nText(i18ns: I18n[]): string {
    return this.i18nService.getText(i18ns);
  }

  compareFn = (o1: Faq, o2: Faq): boolean => {
    if (!o1 || !o2) {
      return false;
    }

    return o1.id === o2.id;
  };

  focusToInput(): void {
    this.focus = true
    setTimeout(() => { document.getElementById("text-editor").focus() }, 500)
  }

}
