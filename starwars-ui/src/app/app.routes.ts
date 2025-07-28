import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'people', loadComponent: () => import('./people/people.component').then(m => m.PeopleComponent) },
  { path: 'planets', loadComponent: () => import('./planets/planets.component').then(m => m.PlanetsComponent) }
  // { path: 'ships', component: ShipsComponent },
  // { path: 'movies', component: MoviesComponent },
];
