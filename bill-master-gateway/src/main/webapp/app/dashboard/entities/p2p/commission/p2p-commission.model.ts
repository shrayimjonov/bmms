import { NzTableSortOrder } from 'ng-zorro-antd/table';

export class P2pCommission {
  constructor(
    public id?: number,
    public name?: string,
    public up?: number,
    public down?: number,
    public fee?: number,
    public description?: string,
    public sortOrder?: any,
    public filterMultiple?: boolean,
    public sortDirections?: NzTableSortOrder[]
  ) {}
}
