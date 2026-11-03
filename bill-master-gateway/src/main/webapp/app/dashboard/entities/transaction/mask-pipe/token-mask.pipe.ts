import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'tokenMask',
})
export class TokenMask implements PipeTransform {
  startsString: string;
  startsNumber: number;

  transform(token: string): string {
    if (token && token.padStart(1, this.startsString) && token.length > 20) {
      return token.replace(token.substring(6, token.length - 6), '******');
    }

    return token;
  }
}
