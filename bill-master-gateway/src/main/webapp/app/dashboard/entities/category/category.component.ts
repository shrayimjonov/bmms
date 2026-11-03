import {Component, OnDestroy, OnInit} from '@angular/core';
import {CategoryService} from './category.service';
import {HttpResponse} from '@angular/common/http';
import {Category} from './category.model';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';
import {ActivatedRoute} from "@angular/router";

@Component({
  templateUrl: 'category.template.html',
})
export class CategoryComponent extends EntityBaseComponent<Category> implements OnInit, OnDestroy {
  readonly exclude = [
    'createdBy',
    'lastModifiedBy',
    'lastModifiedDate',
    'logo',
    'logoFileContentType',
    'names',
    'ord',
    'parent',
    'thumbnail',
    'thumbnailFile',
    'thumbnailFileContentType',
  ];

  constructor(public readonly categoryService: CategoryService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.categoryService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.CATEGORY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.categoryService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Category[]>) => this.onDataReceived(response));
  }
}
