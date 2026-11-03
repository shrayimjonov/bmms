import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';

import {FaqCategory} from './faq-category.model';
import {FaqCategoryService} from './faq-category.service';

import {EntityUpdateBaseComponent} from '../../../../shared/entity/entity-update-base-component';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../../config/event.constants';

@Component({
  templateUrl: 'faq-category-update.template.html',
})
export class FaqCategoryUpdateComponent extends EntityUpdateBaseComponent<FaqCategory, FaqCategoryService> implements OnInit, OnDestroy {
  constructor(
    protected faqCategoryService: FaqCategoryService,
    private readonly activeModal: NgbActiveModal,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {
    super(activeModal, faqCategoryService, Event.FAQ_CATEGORY_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      active: [true],
      names: [null, Validators.required],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const faqCategory: FaqCategory = this.getRawValue();
    this.save(faqCategory);
  }
}
