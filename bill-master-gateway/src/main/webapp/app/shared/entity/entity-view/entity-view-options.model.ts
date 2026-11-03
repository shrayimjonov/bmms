import { IConfig } from 'ngx-mask/lib/config';
import { BmBooleanBadgeType } from '../../directive/bm-boolean-badge.directive';
import { BmFile } from '../../component/bm-file/bm-file.model';

type Type =
  | 'date' // string
  | 'mask' // string | [string, IConfig['patterns']]
  | 'translation' // string
  | 'link' // null
  | 'boolean' // BmBooleanBadgeType
  | 'file' // BmFile
  | 'amount' // AmountRatio
  | 'json'; // null

export class EntityViewOptions {
  title: string;
  value: any | HTMLElement;
  type?: Type;
  date?: string;
  mask?: string | [string, IConfig['patterns']];
  amountRatio?: number;
  translation?: string;
  link?: (v: EntityViewOptions) => void;
  boolean?: BmBooleanBadgeType;
  file?: BmFile;
}
