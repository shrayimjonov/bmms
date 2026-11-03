export enum UserType {
  ADMIN = 'ADMIN',
  AGENT = 'AGENT',
  RECIPIENT = 'RECIPIENT',
}

export class UserGroupAuthority {
  constructor(
    public id?: string,
    public attributes?: string,
    public clientRole?: boolean,
    public composite?: boolean,
    public composites?: string,
    public scopeParamRequired?: string,
    public containerId?: string,
    public name?: string,
    public description?: string
  ) {}
}

export class UserGroup {
  constructor(
    public id?: number,
    public name?: string,
    public active?: boolean,
    public description?: string,
    public authorities?: string[],
    public type?: UserType
  ) {
    this.active = this.active || false;
  }
}
