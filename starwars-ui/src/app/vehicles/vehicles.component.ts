import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { VehicleService } from '../services/vehicle.service';
import { Vehicle } from '../models/vehicle.model';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {buildPagesArray} from '../shared/pagination.util';

@Component({
  selector: 'app-vehicles',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './vehicles.component.html',
  styleUrls: ['./vehicles.component.scss']
})
export class VehiclesComponent implements OnInit {
  private readonly vehicleService = inject(VehicleService);
  private readonly destroyRef = inject(DestroyRef);

  vehicles: Vehicle[] = [];
  selectedVehicle: Vehicle | null = null;

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
    this.loadVehicles();
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.totalElements / this.size);
    this.pages = buildPagesArray(this.page, this.totalPages);
  }

  goToPage(p: number): void {
    if (p === -1 || p === this.page) return;
    this.page = p;
    this.loadVehicles();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadVehicles();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadVehicles();
    }
  }

  loadVehicles(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.vehicleService
      .getAllVehicles(this.page, this.size, this.search, this.sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.vehicles = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.vehicles.length === 0;
          this.isLoading = false;
          this.updatePagination();
        },
        error: () => {
          this.hasError = true;
          this.errorMessage = 'Failed to load vehicles.';
          this.isLoading = false;
        }
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadVehicles();
  }

  changeSort(field: string): void {
    const [currentField, currentDir] = this.sort.split(',');

    let newDir = 'asc';
    if (currentField === field) {
      newDir = currentDir === 'asc' ? 'desc' : 'asc';
    }

    this.sort = `${field},${newDir}`;
    this.page = 0;
    this.loadVehicles();
  }

  openModal(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
  }

  closeModal(): void {
    this.selectedVehicle = null;
  }
}
