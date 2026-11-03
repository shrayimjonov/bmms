import {AfterViewInit, Directive, ElementRef} from "@angular/core";

@Directive({
  selector: '[auto-focus]'
})
export class AutoFocusDirective implements AfterViewInit {
  constructor(private elementRef: ElementRef<HTMLElement>) {
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.elementRef.nativeElement.focus());
  }
}
