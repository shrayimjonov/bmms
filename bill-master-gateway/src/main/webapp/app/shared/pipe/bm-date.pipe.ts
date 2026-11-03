import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';

type BmDate = Date | string | number;

@Pipe({
  name: 'bmDate',
})
export class BmDatePipe implements PipeTransform {
  private readonly format = 'dd.MM.YYYY HH:mm:ss';

  constructor(private datePipe: DatePipe) {}

  transform(value: BmDate | Moment, format = this.format, timezone?: string, locale?: string): string {
    if (value instanceof moment) {
      value = (value as Moment)?.toJSON();
    }

    return this.datePipe.transform(value as BmDate, format, null, locale);
  }
}
