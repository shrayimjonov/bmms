import {Pipe, PipeTransform} from '@angular/core';
import {Util} from '../../../../shared/util';

@Pipe({
  name: 'sortBy',
})
export class SortByPipe implements PipeTransform {
  transform(array: any[], field: string): any[] {
    if (!Array.isArray(array)) {
      return array;
    }

    if (!field) {
      console.error('Field not set');
      return array;
    }

    return Util.sortBy(array, field);
  }
}
