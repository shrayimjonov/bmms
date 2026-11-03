import { NgModule } from '@angular/core';
import { BmJsonViewerComponent } from './bm-json-viewer.component';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [NgJsonEditorModule, FormsModule],
  declarations: [BmJsonViewerComponent],
  exports: [BmJsonViewerComponent],
})
export class BmJsonViewerModule {}
