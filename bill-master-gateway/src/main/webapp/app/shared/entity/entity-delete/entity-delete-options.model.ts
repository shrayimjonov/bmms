import { Observable } from 'rxjs';
import { Event } from '../../../config/event.constants';

export class EntityDeleteOptions {
  useFunction: Observable<any>;
  event: Event;
  alertTranslation: string;
  alertTranslationValue: any;
}
