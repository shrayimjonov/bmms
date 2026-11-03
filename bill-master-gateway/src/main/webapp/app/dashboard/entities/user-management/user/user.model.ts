import { UserGroup } from '../user-group/user-group.model';

export enum StateEnum {
  NEW = 'NEW',
  ACTIVE = 'ACTIVE',
  DELETED = 'DELETED',
}

export class User {
  constructor(
    public id?: number,
    public username?: string,
    public firstName?: string | null,
    public lastName?: string | null,
    public email?: string,
    public enabled?: boolean,
    public langKey?: string,
    public authorities?: string[],
    public createdBy?: string,
    public createdDate?: Date,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Date,
    public password?: string,
    public userGroups?: UserGroup[],
    public profilePictureId?: number,
    public phoneNumber?: string,
    public state?: StateEnum,
    public createdTimestamp?: Date,
    public temporary?: boolean,
    public groups?: string[]
  ) {}
}
