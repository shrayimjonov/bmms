import {Result} from "./result.model";

export class ResultData<DATA> extends Result {
  constructor(public data?: DATA) {
    super();
  }
}
