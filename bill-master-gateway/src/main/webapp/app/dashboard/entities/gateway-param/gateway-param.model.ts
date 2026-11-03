import { RequestType } from '../../../shared/model/enumerations/request-type.model';
import { MethodType } from '../../../shared/model/enumerations/method-type.model';
import { ProtocolType } from '../../../shared/model/enumerations/protocol-type.model';
import { ModeType } from '../../../shared/model/enumerations/mode-type.model';
import { Recipient } from '../recipient/recipient.model';

export class GatewayParam {
  constructor(
    public id?: number,
    public url?: string,
    public type?: RequestType,
    public username?: string,
    public password?: string,
    public token?: string,
    public body?: string,
    public statusQuery?: string,
    public messageQuery?: string,
    public successCode?: string,
    public callback?: string,
    public timeout?: number,
    public headers?: string,
    public dataQueries?: string,
    public httpMethodType?: MethodType,
    public protocol?: ProtocolType,
    public requestMediaType?: string,
    public responseMediaType?: string,
    public active?: boolean,
    public mode?: ModeType,
    public extIdQuery?: string,
    public successOn2xx?: boolean,
    public recipient?: Recipient
  ) {}
}
