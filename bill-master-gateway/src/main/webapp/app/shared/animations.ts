import { animate, style, transition, trigger } from '@angular/animations';

export const fadeIn = trigger('animations', [
  // route 'enter' transition
  transition(':enter', [
    // css styles at start of transition
    style({ opacity: 0 }),

    // animation and styles at end of transition
    animate('.3s', style({ opacity: 1 })),
  ]),
  transition(':leave', [style({ opacity: 1 }), animate('.2s', style({ opacity: 0 }))]),
]);
