import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { Character } from '../models/character.model';
import { CharacterForm } from '../models/character-form.model';
import { CharacterService } from '../services/character.service';
import { CharacterEditFormComponent } from './character-edit-form.component';
import { CreateCharacterFormComponent } from './create-character-form.component';
import { confirm } from '../shared/confirm';
import {buildPagesArray} from '../shared/pagination.util';

@Component({
  selector: 'app-character',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterModule,
    CharacterEditFormComponent,
    CreateCharacterFormComponent
  ],
  templateUrl: './character.component.html',
  styleUrls: ['./character.component.scss']
})
export class CharacterComponent implements OnInit {

  private readonly characterService = inject(CharacterService);
  private readonly destroyRef = inject(DestroyRef);

  isLoading = false;
  hasError = false;
  errorMessage = '';
  isEmpty = false;

  people: Character[] = [];
  films: { id: number; title: string }[] = [];
  species: { id: number; name: string }[] = [];
  vehicles: { id: number; name: string }[] = [];
  starships: { id: number; name: string }[] = [];
  planets: { id: number; name: string }[] = [];

  page = 0;
  size = 15;
  totalElements = 0;
  totalPages = 0;
  pages: number[] = [];

  selectedPerson: Character | null = null;

  search = '';
  sort = '';

  editForm: CharacterForm | null = null;
  createMode = false;

  createForm: CharacterForm = {
    name: '',
    birthYear: '',
    gender: '',
    height: '',
    mass: '',
    hairColor: '',
    skinColor: '',
    eyeColor: '',
    homeworldId: null,
    homeworld: null,
    filmIds: [],
    films: [],
    speciesIds: [],
    species: [],
    vehicleIds: [],
    vehicles: [],
    starshipIds: [],
    starships: [],
    created: null,
    edited: null,
    url: ''
  };

  ngOnInit(): void {
    this.sort = 'name,asc';
    this.loadSupportData();
    this.loadPeople();
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.totalElements / this.size);
    this.pages = buildPagesArray(this.page, this.totalPages);
  }

  goToPage(p: number): void {
    if (p === -1 || p === this.page) return;
    this.page = p;
    this.loadPeople();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadPeople();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadPeople();
    }
  }

  loadPeople(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.characterService
      .getAllPeople(this.page, this.size, this.search, this.sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.people = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.people.length === 0;
          this.isLoading = false;
          this.updatePagination();
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load characters.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadPeople();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');

    let newDir = 'asc';
    if (currentField === field) {
      newDir = currentDir === 'asc' ? 'desc' : 'asc';
    }

    this.sort = `${field},${newDir}`;
    this.page = 0;
    this.loadPeople();
  }

  openModal(person: Character): void {
    this.selectedPerson = person;
  }

  closeModal(): void {
    this.selectedPerson = null;
  }

  editCharacter(person: Character): void {
    this.editForm = {
      id: person.id,
      name: person.name,
      birthYear: person.birthYear,
      gender: person.gender,
      height: person.height,
      mass: person.mass,
      hairColor: person.hairColor,
      skinColor: person.skinColor,
      eyeColor: person.eyeColor,
      homeworldId: person.homeworldId,
      homeworld: person.homeworld,
      filmIds: this.parseInputArray(person.filmIds),
      films: person.films,
      speciesIds: this.parseInputArray(person.speciesIds),
      species: person.species,
      vehicleIds: this.parseInputArray(person.vehicleIds),
      vehicles: person.vehicles,
      starshipIds: this.parseInputArray(person.starshipIds),
      starships: person.starships,
      created: person.created,
      edited: person.edited,
      url: person.url
    };
  }

  saveEdit(updated: CharacterForm): void {
    if (!updated.id) return;
    this.characterService
      .updateCharacter(updated.id, updated)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.editForm = null;
          this.loadPeople();
        },
        error: () => alert('Failed to update character')
      });
  }

  cancelEdit(): void {
    this.editForm = null;
  }

  loadSupportData(): void {
    this.characterService
      .getSupportData()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.films = data.films;
          this.species = data.species;
          this.vehicles = data.vehicles;
          this.starships = data.starships;
          this.planets = data.planets;
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load support data.';
        }
      });
  }

  openCreate(): void {
    this.createForm = {
      name: '',
      birthYear: '',
      gender: '',
      height: '',
      mass: '',
      hairColor: '',
      skinColor: '',
      eyeColor: '',
      homeworldId: null,
      homeworld: null,
      filmIds: [],
      films: [],
      speciesIds: [],
      species: [],
      vehicleIds: [],
      vehicles: [],
      starshipIds: [],
      starships: [],
      created: null,
      edited: null,
      url: ''
    };
    this.createMode = true;
  }

  saveCreate(newChar: CharacterForm): void {
    this.characterService
      .createCharacter(newChar)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.createMode = false;
          this.loadPeople();
        },
        error: () => alert('Failed to create character')
      });
  }

  cancelCreate(): void {
    this.createMode = false;
  }

  private parseInputArray(value: any): number[] {
    if (Array.isArray(value)) return value.map(Number).filter(n => !isNaN(n));
    if (typeof value === 'string') {
      return value
        .split(',')
        .map(v => v.trim())
        .filter(v => v !== '')
        .map(Number)
        .filter(n => !isNaN(n));
    }
    return [];
  }

  async deleteCharacter(id: number | null): Promise<void> {
    if (!id) return;
    const ok = await confirm('Are you sure you want to delete this character?');
    if (!ok) return;

    this.characterService.deleteCharacter(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadPeople(),
        error: () => alert('Failed to delete character')
      });
  }
}
