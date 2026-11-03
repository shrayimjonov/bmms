import { I18n } from '../../../shared/model/pkbillms/i18n.model';
import { Field } from '../field/field.model';

export class FieldValue {
  constructor(
    public id?: number,
    public value?: string,
    public amount?: number,
    public filterBy?: string,
    public ord?: number,
    public field?: Field,
    public titles?: I18n[],
    public descriptions?: I18n[]
  ) {}
}
