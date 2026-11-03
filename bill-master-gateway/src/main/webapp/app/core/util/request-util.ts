import { HttpParams } from '@angular/common/http';
import * as moment from 'moment';

export const createRequestOption = (req?: any): HttpParams => {
  let options: HttpParams = new HttpParams();

  if (req) {
    Object.keys(req).forEach(key => {
      if (key === 'sort') return;

      if (req[key] instanceof moment || req[key] instanceof Date) {
        options = options.set(key, moment(req[key]).toJSON());
        return;
      }

      options = options.set(key, req[key]);
    });

    if (req.sort) {
      req.sort.forEach((val: string) => {
        options = options.append('sort', val);
      });
    }
  }

  return options;
};
