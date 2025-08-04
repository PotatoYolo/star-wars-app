import { Component, OnInit, inject } from '@angular/core';
import { FilmService } from '../services/film.service';
import { Film } from '../models/film.model';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {buildPagesArray} from '../shared/pagination.util';

@Component({
  selector: 'app-films',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './film.component.html',
  styleUrls: ['./film.component.scss']
})
export class FilmComponent implements OnInit {
  private readonly filmService = inject(FilmService);

  films: Film[] = [];
  selectedFilm: Film | null = null;

  isLoading = false;
  hasError = false;
  errorMessage = '';
  isEmpty = false;

  page = 0;
  size = 15;
  totalElements = 0;
  totalPages = 0;
  pages: number[] = [];

  search = '';
  sort = 'title,asc';

  ngOnInit(): void {
    this.loadFilms();
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.totalElements / this.size);
    this.pages = buildPagesArray(this.page, this.totalPages);
  }

  goToPage(p: number): void {
    if (p === -1 || p === this.page) return;
    this.page = p;
    this.loadFilms();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadFilms();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadFilms();
    }
  }

  loadFilms(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.filmService
      .getAllFilms(this.page, this.size, this.search, this.sort)
      .subscribe({
        next: data => {
          this.films = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.films.length === 0;
          this.isLoading = false;
          this.updatePagination();
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load films.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadFilms();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');

    let newDir = 'asc';
    if (currentField === field) {
      newDir = currentDir === 'asc' ? 'desc' : 'asc';
    }

    this.sort = `${field},${newDir}`;
    this.page = 0;
    this.loadFilms();
  }


  openModal(film: Film): void {
    this.selectedFilm = film;
  }

  closeModal(): void {
    this.selectedFilm = null;
  }
}
