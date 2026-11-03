import { Lang } from '../../../config/language.constants';

export enum ReportFormat {
  xlsx = 'xlsx',
  pdf = 'pdf',
  docx = 'docx',
}

export class Report {
  constructor(
    public id?: number,
    public title?: string,
    public fileName?: string,
    public desc?: string,
    public lang?: Lang,
    public templateContentType?: string,
    public template?: any,
    public enabled?: boolean,
    public roles?: string,
    public defaultFormat?: ReportFormat,
    public ord?: number
  ) {}
}
