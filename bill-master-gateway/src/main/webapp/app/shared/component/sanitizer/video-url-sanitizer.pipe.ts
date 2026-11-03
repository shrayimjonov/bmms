import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';

@Pipe({
  name: 'videoUrlSanitizer'
})
export class VideoUrlSanitizerPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {
  }

  transform(url: string): SafeResourceUrl | null {
    if (!url) {
      return null;
    }
    const splitUrl = url.split(/[=&]/)[1];
    return this.sanitizer.bypassSecurityTrustResourceUrl('https://www.youtube.com/embed/' + splitUrl)
  }
}
