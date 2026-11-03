import {Directive, ElementRef, OnDestroy, OnInit, Renderer2} from '@angular/core';
import {NgControl} from '@angular/forms';
import {distinctUntilChanged} from 'rxjs/operators';
import {Subscription} from 'rxjs/internal/Subscription';
import {EventManager} from "../../../core/util/event-manager.service";
import {Event} from "../../../config/event.constants";

@Directive({
  selector: '[input-validator]',
})
export class InputValidatorDirective implements OnInit, OnDestroy {
  private subscribers = new Subscription();
  private color = {
    default: '#d9d9d9',
    danger: '#ff4d4f',
  };

  constructor(
    private eventManager: EventManager,
    private elementRef: ElementRef,
    private renderer: Renderer2,
    private control: NgControl
  ) {
  }

  ngOnInit(): void {
    this.subscription = this.control.valueChanges?.pipe(distinctUntilChanged()).subscribe(() => this.updateBorder());
    this.registerOnSubmit();
  }

  private updateBorder(onSubmit?: boolean): void {
    const control = this.control;
    let color = this.color.default;

    if (control.invalid && (onSubmit || control.dirty || control.touched)) {
      color = this.color.danger;
    }

    this.renderer.setStyle(this.elementRef.nativeElement, 'border', `1px solid ${color}`);
  }

  onSubmit(): void {
    this.updateBorder(true);
  }

  private registerOnSubmit(): void {
    this.subscription = this.eventManager.subscribe(Event.INPUT_VALIDATOR, () => this.onSubmit());
  }

  private set subscription(subscription: Subscription) {
    this.subscribers.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscribers.unsubscribe();
  }
}
