import {Pipe, PipeTransform} from '@angular/core';
import {CategoryProxy} from '../model/category-proxy.model';
import {Util} from "../../../../shared/util";

@Pipe({
  name: 'categoriesFilter',
})
export class CategoriesFilterPipe implements PipeTransform {
  transform(categories: CategoryProxy[], searchText: string): CategoryProxy[] {
    if (!Array.isArray(categories) || !searchText) {
      return categories;
    }

    categories = categories.filter(category =>
      category.recipients.some(recipient => recipient.shortName?.toLowerCase().includes(searchText.toLowerCase()))
    );

    return Util.sortBy(categories, 'ord');
  }
}
