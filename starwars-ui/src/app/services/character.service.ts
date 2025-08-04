import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Character } from '../models/character.model';
import { CharacterForm } from '../models/character-form.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CharacterService {
  private readonly apiUrl = `${environment.apiUrl}/characters`;

  constructor(private readonly http: HttpClient) {}

  getAllPeople(page: number, size: number, search?: string, sort?: string): Observable<Page<Character>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    if (sort) params = params.set('sort', sort);
    return this.http.get<Page<Character>>(this.apiUrl, { params });
  }

  updateCharacter(id: number, dto: CharacterForm): Observable<Character> {
    return this.http.put<Character>(`${this.apiUrl}/${id}`, dto);
  }

  deleteCharacter(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createCharacter(character: CharacterForm): Observable<CharacterForm> {
    return this.http.post<CharacterForm>(this.apiUrl, character);
  }

  getSupportData() {
    return this.http.get<{
      films:    { id: number; title: string }[],
      species:  { id: number; name:  string }[],
      vehicles: { id: number; name:  string }[],
      starships:{ id: number; name:  string }[],
      planets:  { id: number; name:  string }[]
    }>(`${this.apiUrl}/support-data`);
  }
}
