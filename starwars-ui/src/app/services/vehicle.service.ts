import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Vehicle } from '../models/vehicle.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private readonly apiUrl = `${environment.apiUrl}/vehicles`;

  constructor(private readonly http: HttpClient) {}

  getAllVehicles(page: number, size: number, search: string, sort: string): Observable<Page<Vehicle>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('search', search || '');

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<Page<Vehicle>>(this.apiUrl, { params });
  }
}
