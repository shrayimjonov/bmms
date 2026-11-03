import {FaqCategory} from "./category/faq-category.model";
import {I18n} from "../../../shared/model/pkbillms/i18n.model";

export class Faq {
  constructor(
    public id?: number,
    public active?: boolean,
    public videoUrl?: string,
    public questions?: I18n[],
    public answers?: I18n[],
    public faqCategory?: FaqCategory
  ) {
    this.active = this.active ?? false;
  }
}
