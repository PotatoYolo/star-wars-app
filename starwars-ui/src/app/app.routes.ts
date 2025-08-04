import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'character', loadComponent: () => import('./character/character.component').then(m => m.CharacterComponent) },
  { path: 'planets', loadComponent: () => import('./planets/planets.component').then(m => m.PlanetsComponent) },
  { path: 'species', loadComponent: () => import('./species/species.component').then(m => m.SpeciesComponent) },
  { path: 'starships', loadComponent: () => import('./starships/starships.component').then(m => m.StarshipsComponent) },
  { path: 'vehicles', loadComponent: () => import('./vehicles/vehicles.component').then(m => m.VehiclesComponent) },
  { path: 'films', loadComponent: () => import('./films/film.component').then(m => m.FilmComponent) }
];
