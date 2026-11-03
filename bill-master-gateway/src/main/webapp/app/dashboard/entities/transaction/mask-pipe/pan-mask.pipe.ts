import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'panMask',
})
export class PanMaskPipe implements PipeTransform {
  transform(pan: string): string {
    if (pan && (pan.startsWith('8600') || pan.startsWith('9860'))) {
      return pan.replace(pan.substring(6, 12), '******');
    }

    return pan;
  }
}
