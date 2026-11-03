import {Moment} from "moment";
import {CardType} from "../reconcile-summary.model";

export interface P2pReconcile {
  type: CardType;
  fromDate: Moment;
  toDate: Moment;
}
