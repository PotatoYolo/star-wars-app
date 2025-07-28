import { Component, OnInit, inject } from '@angular/core';
import { Person } from '../models/person.model';
import { PeopleService } from '../services/people.service';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-people',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterModule],
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  private service = inject(PeopleService);

  isLoading = false;
  hasError = false;
  errorMessage = '';
  isEmpty = false;
  people: Person[] = [];
  page = 0;
  size = 15;
  totalElements = 0;

  search = '';
  sort = '';

  ngOnInit(): void {
    this.sort = 'name,asc';
    this.loadPeople();
  }

  loadPeople(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.isEmpty = false;

    this.service.getAllPeople(this.page, this.size, this.search, this.sort)
      .subscribe({
        next: (data) => {
          this.people = data.content;
          this.totalElements = data.totalElements;
          this.isEmpty = this.people.length === 0;
          this.isLoading = false;
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

  nextPage(): void {
    this.page++;
    this.loadPeople();
  }

  prevPage(): void {
    if (this.page > 0) this.page--;
    this.loadPeople();
  }

  changeSort(field: string) {
    const [currentField, currentDir] = this.sort.split(',');

    if (currentField !== field) {
      this.sort = `${field},asc`;
    } else {
      this.sort = `${field},${currentDir === 'asc' ? 'desc' : 'asc'}`;
    }

    this.page = 0;
    this.loadPeople();
  }
}
