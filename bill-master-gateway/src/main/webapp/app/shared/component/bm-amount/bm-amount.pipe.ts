import {Pipe, PipeTransform} from '@angular/core';
import {MaskPipe} from 'ngx-mask';
import {BmAmountService} from './bm-amount.service';

@Pipe({
  name: 'bmAmount',
})
export class BmAmountPipe implements PipeTransform {
  constructor(private readonly maskPipe: MaskPipe, private readonly amountService: BmAmountService) {}

  transform(value: number, ratio?: number): string | number {
    if (!ratio) {
      ratio = 100;
    }

    const amount = this.amountService.toModel(value, ratio);

    return this.maskPipe.transform(amount, 'separator.2');
  }
}
