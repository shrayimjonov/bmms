import { Directive, ElementRef, Input, OnInit } from '@angular/core';

@Directive({
  selector: '[image-loader]',
})
export class ImageLoaderDirective implements OnInit {
  @Input() noImagePath = '../../../../content/images/no_image.png';

  constructor(private readonly elRef: ElementRef<HTMLImageElement>) {}

  ngOnInit(): void {
    const img = this.elRef.nativeElement;
    img.onerror = () => (img.src = this.noImagePath);
  }
}
