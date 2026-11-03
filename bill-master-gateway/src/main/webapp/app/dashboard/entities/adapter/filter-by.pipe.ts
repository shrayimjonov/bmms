import { Pipe, PipeTransform } from '@angular/core';

// noinspection AngularMissingOrInvalidDeclarationInModule
@Pipe({
  name: 'filterBy',
})
export class FilterByPipe implements PipeTransform {
  transform(items: any[], field: string, value: any): any[] {
    if (!items || !value || !field) {
      return items;
    }

    return items.filter(item => item[field]?.toLowerCase().indexOf(value.toLowerCase()) !== -1);
  }
}
