import {Observable, of} from 'rxjs';
// import {Buffer} from 'buffer';
import {ActivatedRoute, Router} from '@angular/router';
import {BmFile} from "./component/bm-file/bm-file.model";
import {Type} from "@angular/core";
import {InjectorInstance} from "../app.module";

export class Util {
  static convertEnumToArray(enumClass: any): any {
    return Object.keys(enumClass).map(key => enumClass[key]);
  }

  static isNullOrUndefined<T>(obj: T | null | undefined): obj is null | undefined {
    return typeof obj === 'undefined' || obj === null;
  }

  static isObjectEqual(o1: any, o2: any): boolean {
    return JSON.stringify(o1) === JSON.stringify(o2);
  }

  static distinctObject<T>(array: T[], property: string): T[] {
    return array.filter((object, index, self) => self.map(selfObject => selfObject[property]).indexOf(object[property]) === index);
  }

  static distinct<T>(array: T[]): T[] {
    return array.filter((value, index, self) => self.indexOf(value) === index);
  }

  static extractJsonData(file: File): Observable<any> {
    return new Observable(observer => {
      const fileReader = new FileReader();
      fileReader.readAsText(new Blob([file]), 'UTF-8');

      fileReader.onload = () => {
        const result = fileReader.result as string;

        if (!Util.isParsable(result)) {
          return observer.error('JSON not parsable');
        }

        observer.next(JSON.parse(result));
      };

      fileReader.onerror = error => observer.error(error);
    });
  }

  static isParsable(jsonStr: string): boolean {
    try {
      return JSON.parse(jsonStr);
    } catch (e) {
      return false;
    }
  }

  static sortBy<T>(array: T[], property: string): T[] {
    array.sort((a: T, b: T) => {
      if (a[property] < b[property]) return -1;
      if (a[property] > b[property]) return 1;
      else return 0;
    });

    return array;
  }

  // static encode(str: string): string {
  //   return Buffer.from(encodeURI(str), 'binary').toString('base64');
  // }
  //
  // static decode(str: string): string {
  //   return decodeURI(Buffer.from(str, 'base64').toString('binary'));
  // }

  static generateId(): string {
    const s4 = () =>
      Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);

    return `${s4()}-${s4()}-${s4()}-${s4()}`;
  }

  static isEmpty(obj: any): boolean {
    return Object.keys(obj)?.length === 0;
  }

  static getClassName(instance: unknown): string | undefined {
    if (!instance) {
      return undefined;
    }

    return instance.constructor.name;
  }

  static createInstance<A>(c: new () => A): A {
    return new c();
  }

  static isPresent<T>(t: T | undefined | null | void): t is T {
    return t !== undefined && t !== null;
  }

  static getLastRoute(route: ActivatedRoute): ActivatedRoute {
    const child = route.children[0];
    if (child.children.length) {
      return this.getLastRoute(child);
    }

    return child;
  }

  static getLocationParams(): URLSearchParams | null {
    const indexOfParams = window.location.href.indexOf('?');
    if (~indexOfParams) {
      return new URLSearchParams(window.location.href.substring(indexOfParams, window.location.href.length))
    }

    return null;
  }

  static readAsDataUrl(file: File): Observable<BmFile | null> {
    if (!file) return of(null);

    return new Observable(observer => {
      const reader = new FileReader();
      reader.readAsDataURL(file);

      reader.onload = () => {
        observer.next({
          name: file.name,
          contentType: file.type,
          content: (reader.result as string).split(',')[1],
        });

        observer.complete();
      };

      reader.onerror = () => {
        observer.error(null);
        observer.complete();
      };
    });
  }

  static inject<T>(service: Type<T>): T {
    return InjectorInstance.get<T>(service);
  }

  static getRouteUrlWithoutParams(router: Router): string | null {
    if (!router?.url) {
      return null;
    }

    return router.url.split('?')[0];
  }
}
