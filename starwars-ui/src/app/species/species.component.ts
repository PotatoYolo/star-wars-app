import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { SpeciesService } from '../services/species.service';
import { Species } from '../models/species.model';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {buildPagesArray} from '../shared/pagination.util';

@Component({
  selector: 'app-species',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './species.component.html',
  styleUrls: ['./species.component.scss']
})
export class SpeciesComponent implements OnInit {
  private readonly speciesService = inject(SpeciesService);
  private readonly destroyRef = inject(DestroyRef);

  speciesList: Species[] = [];
  selectedSpecies: Species | null = null;

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
    this.loadSpecies();
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.totalElements / this.size);
    this.pages = buildPagesArray(this.page, this.totalPages);
  }

  goToPage(p: number): void {
    if (p === -1 || p === this.page) return;
    this.page = p;
    this.loadSpecies();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadSpecies();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadSpecies();
    }
  }

  loadSpecies(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.speciesService
      .getAllSpecies(this.page, this.size, this.search, this.sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.speciesList = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.speciesList.length === 0;
          this.isLoading = false;
          this.updatePagination();
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load species.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadSpecies();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');

    let newDir = 'asc';
    if (currentField === field) {
      newDir = currentDir === 'asc' ? 'desc' : 'asc';
    }

    this.sort = `${field},${newDir}`;
    this.page = 0;
    this.loadSpecies();
  }


  openModal(species: Species): void {
    this.selectedSpecies = species;
  }

  closeModal(): void {
    this.selectedSpecies = null;
  }
}
