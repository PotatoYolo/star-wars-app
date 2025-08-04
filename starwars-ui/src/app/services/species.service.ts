import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Species } from '../models/species.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SpeciesService {
  private readonly apiUrl = `${environment.apiUrl}/species`;

  constructor(private readonly http: HttpClient) {}

  getAllSpecies(page: number, size: number, search: string, sort: string): Observable<Page<Species>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('search', search || '');

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<Page<Species>>(this.apiUrl, { params });
  }
}
