import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { CharacterForm } from '../models/character-form.model';

@Component({
  selector: 'app-create-character-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NgSelectModule],
  templateUrl: './create-character-form.component.html',
  styleUrls: ['./create-character-form.component.scss']
})
export class CreateCharacterFormComponent {
  @Input() character: CharacterForm | null = null;
  @Input() films: { id: number; title: string }[] = [];
  @Input() species: { id: number; name: string }[] = [];
  @Input() vehicles: { id: number; name: string }[] = [];
  @Input() starships: { id: number; name: string }[] = [];
  @Input() planets: { id: number; name: string }[] = [];

  @Output() save = new EventEmitter<CharacterForm>();
  @Output() cancel = new EventEmitter<void>();

  submitForm(): void {
    if (this.character) {
      const parsed = {
        ...this.character,
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
