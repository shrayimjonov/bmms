import {ChangeDetectionStrategy, Component, Input} from "@angular/core";
import {DataUtils} from "../../../core/util/data-util.service";

@Component({
  selector: '[bm-table-image-viewer]',
  templateUrl: 'bm-table-image-viewer.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BmTableImageViewerComponent {
  @Input() bmContent: any;
  @Input() bmContentType: string;

  constructor(private dataUtils: DataUtils) {
  }

  open(): void {
    this.dataUtils.openFile(this.bmContentType, this.bmContent);
  }

  getImageSrc(): string {
    if (this.bmContent && this.bmContentType) {
      return `data:${this.bmContentType};base64,${this.bmContent}`;
    }

    return null;
  }
}
