import {Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CharacterForm } from '../models/character-form.model';
import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: 'app-character-edit-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NgSelectModule],
  templateUrl: './character-edit-form.component.html',
  styleUrls: ['./character-edit-form.component.scss']
})
export class CharacterEditFormComponent {
  @Input() character: CharacterForm | null = null;
  @Input() films: { id: number; title: string }[] = [];
  @Input() species: { id: number; name: string }[] = [];
  @Input() vehicles: { id: number; name: string }[] = [];
  @Input() starships: { id: number; name: string }[] = [];
  @Input() planets: { id: number; name: string }[] = [];

  @Output() save = new EventEmitter<CharacterForm>();
  @Output() cancel = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges): void {
    console.log('🚨 Changes in Edit Form:');
    if (changes['species']) {
      console.log('Species received:', this.species);
    }
    if (changes['films']) {
      console.log('Films received:', this.films);
    }
  }
  submitForm(): void {
    if (this.character) {
      const parsed = {
        ...this.character,
        id: this.character.id ?? undefined,
        filmIds: this.parseInputArray(this.character.filmIds),
        speciesIds: this.parseInputArray(this.character.speciesIds),
        vehicleIds: this.parseInputArray(this.character.vehicleIds),
        starshipIds: this.parseInputArray(this.character.starshipIds)
      };

      this.save.emit(parsed);
    }
  }

  private parseInputArray(value: any): number[] {
    if (Array.isArray(value)) return value;
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

  cancelEdit(): void {
    this.cancel.emit();
  }
}
