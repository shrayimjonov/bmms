import { UserGroup } from '../../dashboard/entities/user-management/user-group/user-group.model';

export class Account {
  constructor(
    public activated: boolean,
    public authorities: string[],
    public email: string,
    public firstName: string | null,
    public langKey: string,
    public lastName: string | null,
    public login: string,
    public imageUrl: string | null,
    public userGroups: UserGroup[]
  ) {}
}
