import {Pipe, PipeTransform} from "@angular/core";

@Pipe({
  name: 'bmEllipsis'
})
export class BmEllipsisPipe implements PipeTransform {
  transform(value: string, limit = 10): any | null {
    if (!value) {
      return null;
    }

    if (value?.length > limit) {
      return `${value.substr(0, limit)}...`
    }

    return value;
  }
}
