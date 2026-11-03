export class RecipientFileSchema {
  constructor(public id?: number, public name?: string, public idList?: string, public root?: string | null) {}
}

export function getRecipientFileSchemaIdentifier(recipientFileSchema: RecipientFileSchema): number | undefined {
  return recipientFileSchema.id;
}
