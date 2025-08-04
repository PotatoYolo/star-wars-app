import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { StarshipService } from '../services/starship.service';
import { Starship } from '../models/starship.model';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {buildPagesArray} from '../shared/pagination.util';

@Component({
  selector: 'app-starships',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './starships.component.html',
  styleUrls: ['./starships.component.scss']
})
export class StarshipsComponent implements OnInit {
  private readonly starshipService = inject(StarshipService);
  private readonly destroyRef = inject(DestroyRef);

  starships: Starship[] = [];
  selectedStarship: Starship | null = null;

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
  sort = 'name,asc';

  ngOnInit(): void {
    this.loadStarships();
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.totalElements / this.size);
    this.pages = buildPagesArray(this.page, this.totalPages);
  }

  goToPage(p: number): void {
    if (p === -1 || p === this.page) return;
    this.page = p;
    this.loadStarships();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadStarships();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadStarships();
    }
  }

  loadStarships(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.starshipService
      .getAllStarships(this.page, this.size, this.search, this.sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.starships = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.starships.length === 0;
          this.isLoading = false;
          this.updatePagination();
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load starships.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadStarships();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');

    let newDir = 'asc';
    if (currentField === field) {
      newDir = currentDir === 'asc' ? 'desc' : 'asc';
    }

    this.sort = `${field},${newDir}`;
    this.page = 0;
    this.loadStarships();
  }


  openModal(starship: Starship): void {
    this.selectedStarship = starship;
  }

  closeModal(): void {
    this.selectedStarship = null;
  }
}
