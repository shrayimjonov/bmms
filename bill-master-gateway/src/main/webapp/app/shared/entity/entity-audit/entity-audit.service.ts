import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { EntityAuditEvent } from './entity-audit-event.model';

@Injectable({ providedIn: 'root' })
export class EntityAuditService {
  constructor(private http: HttpClient) {}

  getAllAudited(): Observable<string[]> {
    return this.http.get<string[]>('api/audits/entity/all');
  }

  findByEntity(entity: string, limit: number): Observable<HttpResponse<EntityAuditEvent[]>> {
    const params: HttpParams = new HttpParams().append('entityType', entity).append('limit', limit.toString());

    return this.http.get<EntityAuditEvent[]>('api/audits/entity/changes', {
      params,
      observe: 'response',
    });
  }

  getPrevVersion(qualifiedName: string, entityId: string, commitVersion: number): Observable<HttpResponse<EntityAuditEvent>> {
    const params: HttpParams = new HttpParams()
      .append('qualifiedName', qualifiedName)
      .append('entityId', entityId)
      .append('commitVersion', commitVersion.toString());

    return this.http.get<EntityAuditEvent>('api/audits/entity/changes/version/previous', {
      params,
      observe: 'response',
    });
  }

  getHistoryByEntityId(qualifiedName: string, entityId: string): Observable<any> {
    const params: HttpParams = new HttpParams().append('qualifiedName', qualifiedName || '').append('entityId', entityId || '-1');

    return this.http.get<any>('/services/bmms/api/audits/entity/history', {
      params,
      observe: 'body',
    });
  }

  getHistoryByEntityId2(): Observable<HttpResponse<any>> {
    return of(
      new HttpResponse({
        body: [
          {
            commitMetadata: {
              author: 'admin',
              properties: [],
              commitDate: '2020-10-06T01:41:31.833',
              commitDateInstant: '2020-10-05T20:41:31.833146100Z',
              id: 7.0,
            },
            globalId: {
              entity: 'uz.devops.hamyon.domain.CommissionSchema',
              cdoId: 1,
            },
            state: {
              instTime: 3,
              name: 'Rustic',
              active: true,
              commission: 88,
              id: 1,
              instTimeUnit: 'MONTHS',
            },
            changedProperties: ['instTimeUnit'],
            type: 'UPDATE',
            version: 4,
          },
          {
            commitMetadata: {
              author: 'admin',
              properties: [],
              commitDate: '2020-10-05T22:15:47.901',
              commitDateInstant: '2020-10-05T17:15:47.901772600Z',
              id: 6.0,
            },
            globalId: {
              entity: 'uz.devops.hamyon.domain.CommissionSchema',
              cdoId: 1,
            },
            state: {
              instTime: 3,
              name: 'Rustic',
              active: true,
              commission: 88,
              id: 1,
              instTimeUnit: 'DAYS',
            },
            changedProperties: ['instTime', 'commission'],
            type: 'UPDATE',
            version: 3,
          },
          {
            commitMetadata: {
              author: 'admin',
              properties: [],
              commitDate: '2020-10-02T19:46:06.415',
              commitDateInstant: '2020-10-02T14:46:06.415265900Z',
              id: 5.0,
            },
            globalId: {
              entity: 'uz.devops.hamyon.domain.CommissionSchema',
              cdoId: 1,
            },
            state: {
              instTime: 27827,
              name: 'Rustic',
              active: true,
              commission: 55,
              id: 1,
              instTimeUnit: 'DAYS',
            },
            changedProperties: ['active', 'commission'],
            type: 'UPDATE',
            version: 2,
          },
          {
            commitMetadata: {
              author: 'admin',
              properties: [],
              commitDate: '2020-10-02T19:44:29.718',
              commitDateInstant: '2020-10-02T14:44:29.718096Z',
              id: 4.0,
            },
            globalId: {
              entity: 'uz.devops.hamyon.domain.CommissionSchema',
              cdoId: 1,
            },
            state: {
              instTime: 27827,
              name: 'Rustic',
              active: false,
              commission: 15,
              id: 1,
              instTimeUnit: 'DAYS',
            },
            changedProperties: ['instTime', 'name', 'active', 'commission', 'id', 'instTimeUnit'],
            type: 'INITIAL',
            version: 1,
          },
        ],
      })
    );
  }
}
