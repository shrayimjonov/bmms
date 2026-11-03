export class Gateway {
  constructor(
    public id?: number,
    public name?: string,
    public org?: string,
    public active?: boolean,
    public mfo?: string,
    public taxnr?: number,
    public ban?: string,
    public contract?: string,
    public springBeanName?: string,
    public depositId?: number
  ) {}
}
