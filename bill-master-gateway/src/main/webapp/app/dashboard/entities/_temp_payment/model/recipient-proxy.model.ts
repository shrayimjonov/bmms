import {Recipient} from '../../recipient/recipient.model';
import {Field} from '../../field/field.model';

export class RecipientProxy extends Recipient {
  constructor(public fields?: Field[]) {
    super();
  }
}
