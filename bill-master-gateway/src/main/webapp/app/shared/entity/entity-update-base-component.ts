import {Subject} from "rxjs";
import {AbstractControlOptions, FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Event} from "../../config/event.constants";
import {EventManager} from "../../core/util/event-manager.service";
import {finalize, takeUntil} from "rxjs/operators";
import {BmAlertService} from "../component/bm-alert/bm-alert.service";
import {Util} from "../util";

export abstract class EntityUpdateBaseComponent<ENTITY, SERVICE> {
  readonly data: ENTITY;
  readonly duplicate: boolean;

  protected readonly destroy$ = new Subject<void>();

  loading: boolean;

  editForm: FormGroup;

  private readonly _alertService = Util.inject(BmAlertService);

  protected constructor(private readonly _activeModal: NgbActiveModal,
                        private readonly _service: SERVICE,
                        private readonly _modifyEvent: Event) {
  }

  protected createEditForm(controlsConfig: { [key: string]: any }, options?: AbstractControlOptions | null): void {
    this.editForm = Util.inject(FormBuilder).group(controlsConfig, options);

    const copy = {...this.data};
    if (copy['id']) {
      if (this.duplicate) {
        copy['id'] = null;
      }

      this.editForm.patchValue(copy, {emitEvent: false});
    }
  }

  protected getRawValue(): ENTITY {
    return this.editForm.getRawValue();
  }

  save(entity?: ENTITY, method?: keyof SERVICE, cb?: () => void): void {
    entity = entity || this.getRawValue();
    this.loading = true;
    if (entity['id']) {
      this.update(entity, method as string, cb);
    } else {
      this.create(entity, method as string, cb);
    }
  }

  protected create(entity: ENTITY, method?: string, cb?: () => void): void {
    method = method || 'create';
    if (method in this._service) {
      this._service[method](entity)
        .pipe(
          finalize(() => this.loading = false),
          takeUntil(this.destroy$)
        )
        .subscribe(() => this.onSuccess('saved', cb));
    }
  }

  protected update(entity: ENTITY, method?: string, cb?: () => void): void {
    method = method || 'update';
    if (method in this._service) {
      this._service[method](entity)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => this.loading = false)
        )
        .subscribe(() => this.onSuccess('updated', cb));
    }
  }

  private onSuccess(action: 'saved' | 'updated', cb?: () => void): void {
    this._alertService.success('global.data.' + action, null, true);
    Util.inject(EventManager).broadcast(this._modifyEvent);
    this.closeModal();

    if (cb) {
      cb();
    }
  }

  protected unsubscribe(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  closeModal(): void {
    this._activeModal.dismiss('close');
  }
}
