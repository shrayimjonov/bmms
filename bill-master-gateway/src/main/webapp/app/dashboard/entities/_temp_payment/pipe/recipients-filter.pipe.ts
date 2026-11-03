import {Pipe, PipeTransform} from '@angular/core';
import {RecipientProxy} from '../model/recipient-proxy.model';
import {Util} from "../../../../shared/util";

@Pipe({
  name: 'recipientsFilter',
})
export class RecipientsFilterPipe implements PipeTransform {
  transform(recipients: RecipientProxy[], searchText: string): RecipientProxy[] {
    if (!Array.isArray(recipients) || !searchText) {
      return recipients;
    }

    recipients = recipients.filter(recipient => recipient.shortName?.toLowerCase().includes(searchText.toLowerCase()));

    return Util.sortBy(recipients, 'ord');
  }
}
