import {Type} from '@angular/core';
import {NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';

export class EntityUpdateOptions<T> {
  component: Type<T>;
  componentInstances?: Partial<T>;
  modalOptions?: NgbModalOptions;
}
