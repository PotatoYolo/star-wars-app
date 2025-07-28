import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Person } from '../models/person.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PeopleService {
  constructor(private http: HttpClient) {}

  private apiUrl = `${environment.apiUrl}/people`;

  getAllPeople(page: number, size: number, search?: string, sort?: string): Observable<Page<Person>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (search) params = params.set('search', search);
    if (sort) {
      const [field, direction] = sort.split(',');
      params = params.set('sort', `${field},${direction}`);
    }
    return this.http.get<Page<Person>>(this.apiUrl, { params });  }
}
