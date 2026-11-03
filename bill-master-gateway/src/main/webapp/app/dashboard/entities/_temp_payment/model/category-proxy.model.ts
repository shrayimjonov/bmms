import {Category} from '../../category/category.model';
import {Recipient} from '../../recipient/recipient.model';

export class CategoryProxy extends Category {
  constructor(public recipients?: Recipient[], public more?: boolean) {
    super();
  }
}
