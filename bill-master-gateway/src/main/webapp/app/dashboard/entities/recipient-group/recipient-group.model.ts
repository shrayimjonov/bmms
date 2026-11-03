import { Recipient } from '../recipient/recipient.model';

export class RecipientGroup {
  constructor(public id?: number, public name?: string, public active?: boolean, public more?: string, public recipients?: Recipient[]) {}
}
