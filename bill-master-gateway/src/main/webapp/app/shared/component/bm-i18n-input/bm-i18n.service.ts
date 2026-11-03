import {Injectable} from '@angular/core';
import {I18n} from '../../model/pkbillms/i18n.model';
import {LanguageService} from '../../../core/util/language.service';
import {Lang} from '../../../config/language.constants';

@Injectable({ providedIn: 'root' })
export class BmI18nService {
  constructor(private readonly langService: LanguageService) {}

  getText(i18ns: I18n[]): string {
    return i18ns?.find(i18n => i18n.lang === Lang[this.langService.getCurrent().toUpperCase()])?.text;
  }
}
