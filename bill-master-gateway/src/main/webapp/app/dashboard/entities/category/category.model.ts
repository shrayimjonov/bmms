import { I18n } from '../../../shared/model/pkbillms/i18n.model';

export class Category {
  constructor(
    public id?: number,
    public shortName?: string,
    public thumbnail?: string,
    public thumbnailFileContentType?: string,
    public thumbnailFile?: any,
    public logo?: string,
    public logoFileContentType?: string,
    public logoFile?: any,
    public ord?: number,
    public active?: boolean,
    public parent?: Category,
    public names?: I18n[]
  ) {}
}
