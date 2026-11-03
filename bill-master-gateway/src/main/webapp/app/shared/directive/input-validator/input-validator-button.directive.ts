import {Directive, HostListener, Input} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {BmAlertService} from 'app/shared/component/bm-alert/bm-alert.service';
import {EventManager} from "../../../core/util/event-manager.service";
import {Event} from "../../../config/event.constants";

@Directive({
  selector: '[input-validator-button]',
})
export class InputValidatorButtonDirective {
  @Input('input-validator-button') formGroup: FormGroup;
  @Input() error = 'entity.validation.notFilled';

  constructor(private eventManager: EventManager, private alertService: BmAlertService) {
  }

  @HostListener('click', ['$event'])
  onClick(event: PointerEvent): void {
    if (!this.formGroup) {
      // eslint-disable-next-line no-console
      return console.error('FormGroup does not set on directive argument');
    }

    if (this.formGroup.invalid) {
      this.eventManager.broadcast(Event.INPUT_VALIDATOR);
      this.alertService.error(this.error);
      event.preventDefault();
    }
  }
}
