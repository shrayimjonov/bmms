import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ComponentFactoryResolver,
  ComponentRef,
  OnInit,
  Type,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from 'rxjs';
import {pluck, takeUntil} from 'rxjs/operators';
import {Checkout} from './checkout.model';
import {CheckoutUnauthorizedComponent} from './unauthorized/checkout-unauthorized.component';
import {CheckoutAgentComponent} from './agent/checkout-agent.component';
import {PaymentService} from '../dashboard/entities/_temp_payment/payment.service';
import {fadeIn} from '../shared/animations';

type CheckoutComponentType = CheckoutUnauthorizedComponent | CheckoutAgentComponent;

@Component({
  templateUrl: 'checkout.component.html',
  styleUrls: ['checkout.component.scss', 'brand-styles/anorbank.scss'],
  animations: [fadeIn],
})
export class CheckoutComponent implements OnInit, AfterViewInit {
  @ViewChild('checkoutContainer', { read: ViewContainerRef, static: false }) checkoutContainer: ViewContainerRef;

  private readonly destroy$ = new Subject<void>();

  checkout: Checkout;
  draw: boolean;

  private componentRef: ComponentRef<any>;

  constructor(
    public readonly paymentService: PaymentService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly cdr: ChangeDetectorRef,
    private readonly componentResolver: ComponentFactoryResolver
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.pipe(takeUntil(this.destroy$), pluck('checkout')).subscribe((checkout: Checkout) => {
      if (checkout.bill?.params) {
        checkout.bill.params = typeof checkout.bill.params === 'string' ? JSON.parse(checkout.bill.params) : checkout.bill.params;
      }

      this.checkout = checkout;
    });
  }

  ngAfterViewInit(): void {
    this.checkoutContainer.clear();
    const component = this.getCheckoutComponent();
    const componentFactory = this.componentResolver.resolveComponentFactory(component);
    this.componentRef = this.checkoutContainer.createComponent(componentFactory);
    this.componentRef.instance.checkout = this.checkout;
    this.cdr.detectChanges();
  }

  private getCheckoutComponent(): Type<CheckoutComponentType> {
    if (this.checkout.bill) {
      return CheckoutAgentComponent;
    }

    return CheckoutUnauthorizedComponent;
  }
}
