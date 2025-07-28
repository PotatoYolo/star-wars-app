import { Component, OnInit, inject } from '@angular/core';
import { Planet } from '../models/planet.model';
import { PlanetsService } from '../services/planets.service';
import { AsyncPipe, DatePipe, NgIf, NgFor, JsonPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-planets',
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, DatePipe, FormsModule, JsonPipe, RouterModule],
  templateUrl: './planets.component.html',
  styleUrls: ['./planets.component.scss']
})
export class PlanetsComponent implements OnInit {
  private service = inject(PlanetsService);

  isLoading = false;
  hasError = false;
  errorMessage = '';
  isEmpty = false;
  planets: Planet[] = [];
  page = 0;
  size = 15;
  totalElements = 0;

  search = '';
  sort = 'name';

  ngOnInit(): void {
    this.sort = 'name,asc';
    this.loadPlanets();
  }

  loadPlanets(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.service.getAllPlanets(this.page, this.size, this.search, this.sort)
      .subscribe({
        next: (data) => {
          console.log('Backend data:', data);
          this.planets = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.planets.length === 0;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Planet load error:', err);
          this.hasError = true;
          this.errorMessage = 'Failed to load planets.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadPlanets();
  }

  nextPage(): void {
    this.page++;
    this.loadPlanets();
  }

  prevPage(): void {
    if (this.page > 0) this.page--;
    this.loadPlanets();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');
    if (currentField !== field) {
      this.sort = `${field},asc`;
    } else {
      this.sort = `${field},${currentDir === 'asc' ? 'desc' : 'asc'}`;
    }
    this.page = 0;
    this.loadPlanets();
  }
}
