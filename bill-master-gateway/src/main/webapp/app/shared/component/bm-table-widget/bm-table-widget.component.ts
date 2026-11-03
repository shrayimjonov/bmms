import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {NzSizeDSType} from "ng-zorro-antd/core/types/size";
import {ITEMS_PER_PAGE} from "../../../config/pagination.constants";

@Component({
  selector: 'bm-table-widget',
  templateUrl: 'bm-table-widget.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BmTableWidgetComponent implements OnInit {
  @Input() size: NzSizeDSType = 'small';
  @Output() sizeChange = new EventEmitter<NzSizeDSType>();
  @Input() itemsPerPage = ITEMS_PER_PAGE;
  @Output() itemsPerPageChange = new EventEmitter<number>();

  isSmall: boolean;

  ngOnInit(): void {
    this.sizeChange.emit(this.size ?? 'small');
  }

  changeSize(): void {
    this.isSmall = !this.isSmall;
    this.sizeChange.emit(!this.isSmall ? 'small' : 'default');
  }
}
