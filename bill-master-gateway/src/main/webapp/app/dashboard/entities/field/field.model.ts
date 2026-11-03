import { I18n } from 'app/shared/model/pkbillms/i18n.model';
import { FieldValue } from '../field-value/field-value.model';
import { Prefix } from '../prefix/prefix.model';
import { FieldType } from 'app/shared/model/enumerations/field-type.model';
import { Visibility } from 'app/shared/model/enumerations/visibility.model';
import { RequestType } from 'app/shared/model/enumerations/request-type.model';
import { Recipient } from '../recipient/recipient.model';

export enum ControlType {
  TEXT = 'TEXT',
  SELECT = 'SELECT',
  READONLY = 'READONLY',
}

export class Field {
  constructor(
    public id?: number,
    public name?: string,
    public extId?: number,
    public type?: FieldType,
    public controlType?: ControlType,
    public content?: string,
    public validation?: string,
    public placeholder?: string,
    public mask?: string,
    public maskSymbol?: string,
    public lengthMin?: number,
    public lengthMax?: number,
    public requireField?: string,
    public filterBy?: string,
    public optional?: boolean,
    public output?: boolean,
    public visibility?: Visibility,
    public ord?: number,
    public requestType?: RequestType,
    public titles?: I18n[],
    public descriptions?: I18n[],
    public values?: FieldValue[],
    public prefixes?: Prefix[],
    public recipient?: Recipient,
    public fromRequest?: boolean,
    public fromResponse?: boolean
  ) {}
}
