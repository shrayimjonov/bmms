import {HttpMethod} from '../../admin/metrics/metrics.model';
import {I18n} from './pkbillms/i18n.model';

export interface Action {
  readonly title: I18n[],
  readonly uri: string,
  readonly method: HttpMethod,
  readonly active: boolean,
  readonly confirmable: boolean,
  readonly uriParams?: string[],
  readonly color?: string,
  readonly confirmationText?: I18n[],
  readonly description?: I18n[],
  readonly pathVariables?: string[],
  readonly name?: string;
  readonly btnParams?: {};
}
