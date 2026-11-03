import { TranslateService } from '@ngx-translate/core';
import { JsonEditorOptions } from 'ang-jsoneditor';
import { Util } from './util';

type Relation = { id: number };

export function getRelation(object: {}): Relation | null {
  if (!object) {
    return null;
  }

  return { id: object['id'] };
}

export function getRelations(objects: {}[]): Relation[] | null {
  if (!objects?.length) {
    return null;
  }

  return objects.map(object => ({ id: object['id'] }));
}

export function getTranslation(message: string, interpolateParams?: any): string | null {
  if (!message) return null;

  const result = Util.inject(TranslateService).instant(message, interpolateParams);

  if (result.includes('translation-not-found')) {
    return message;
  }

  return result;
}

export function getJsonEditorOptions(): JsonEditorOptions {
  const options = new JsonEditorOptions();
  options.modes = ['code', 'text', 'tree', 'view'];
  options.mode = 'code';

  return options;
}
