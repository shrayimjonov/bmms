import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  template: `
    <nz-result
      nzStatus="success"
      class="p-0 fade-in"
      style="margin-top: 10rem"
      nzTitle="{{ 'payment.paySuccessMsg' | translate }}"
      nzSubTitle="Order number: {{ orderId }}"
    >
      <div nz-result-extra>
        <button
          type="button"
          class="mt-3"
          nz-button
          nzType="primary"
          nzSize="large"
          jhiTranslate="payment.redirectToRecipientUrl"
          (click)="redirect()"
        >
          Redirect
        </button>
      </div>
    </nz-result>
  `,
  styles: [
    `
      :host ::ng-deep .ant-result-icon > .anticon {
        font-size: 60px;
      }
    `,
  ],
})
export class CheckoutSuccessComponent implements OnInit {
  orderId: number | string;
  redirectUrl: string;

  constructor(private readonly router: Router, private readonly activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.orderId = this.activatedRoute.snapshot.queryParamMap.get('orderId');
    this.redirectUrl = this.activatedRoute.snapshot.queryParamMap.get('redirectUrl');
  }

  redirect(): void {
    if (this.redirectUrl) {
      window.location.assign(this.redirectUrl);
    } else {
      this.router.navigate(['']).then();
    }
  }
}
