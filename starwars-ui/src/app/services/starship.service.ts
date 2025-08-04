import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Starship } from '../models/starship.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StarshipService {
  private readonly apiUrl = `${environment.apiUrl}/starships`;

  constructor(private readonly http: HttpClient) {}

  getAllStarships(page: number, size: number, search: string, sort: string): Observable<Page<Starship>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('search', search || '');

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<Page<Starship>>(this.apiUrl, { params });
  }
}
