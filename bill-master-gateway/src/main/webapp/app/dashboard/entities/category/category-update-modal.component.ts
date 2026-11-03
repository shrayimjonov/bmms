import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {CategoryService} from './category.service';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Category} from './category.model';
import {getRelation} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";
import {Event} from "../../../config/event.constants";

@Component({
  templateUrl: 'category-update-modal.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryUpdateModalComponent extends EntityUpdateBaseComponent<Category, CategoryService> implements OnInit, OnDestroy {
  constructor(public readonly categoryService: CategoryService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, categoryService, Event.CATEGORY_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      shortName: [null, [Validators.required, Validators.maxLength(64)]],
      logo: [null],
      logoFile: [null],
      names: [null, Validators.required],
      ord: [null, Validators.required],
      active: [true],
      parent: [null],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const category: Category = this.getRawValue();
    category.parent = getRelation(this.editForm.get('parent').value);
    this.save(category);
  }
}
