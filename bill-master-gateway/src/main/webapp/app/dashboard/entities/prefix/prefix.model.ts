import { PrefixType } from '../../../shared/model/enumerations/prefix-type.model';
import { Field } from '../field/field.model';

export class Prefix {
  constructor(
    public id?: number,
    public type?: PrefixType,
    public value?: string,
    public output?: boolean,
    public requireField?: string,
    public field?: Field
  ) {}
}
