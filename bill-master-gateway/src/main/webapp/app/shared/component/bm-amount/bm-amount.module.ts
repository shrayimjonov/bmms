import { NgModule } from '@angular/core';
import { BmAmountInputComponent } from './bm-amount-input.component';
import { BmAmountPipe } from './bm-amount.pipe';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaskPipe, NgxMaskModule } from 'ngx-mask';
import { NzInputModule } from 'ng-zorro-antd/input';

@NgModule({
  imports: [CommonModule, FormsModule, NgxMaskModule, NzInputModule],
  declarations: [BmAmountInputComponent, BmAmountPipe],
  exports: [BmAmountInputComponent, BmAmountPipe],
  providers: [MaskPipe],
})
export class BmAmountModule {}
