import {I18n} from "../../../../shared/model/pkbillms/i18n.model";
import {Faq} from "../faq.model";

interface FaqCategoryProxy {
  faqs?: Faq[];
  loading?: boolean;
}

export interface FaqCategory extends FaqCategoryProxy {
  id?: number,
  active?: boolean,
  names?: I18n[],
}

export function getFaqCategoryIdentifier(faqCategory: FaqCategory): number | undefined {
  return faqCategory.id;
}
