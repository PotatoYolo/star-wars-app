import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Film } from '../models/film.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FilmService {

  private readonly apiUrl = `${environment.apiUrl}/films`;

  constructor(private readonly http: HttpClient) {}

  getAllFilms(page: number, size: number, search: string, sort: string): Observable<Page<Film>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('search', search || '');

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<Page<Film>>(this.apiUrl, { params });
  }
}
