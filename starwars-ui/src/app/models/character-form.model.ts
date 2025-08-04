export interface CharacterForm {
  id?: number;
  name: string;
  birthYear: string;
  gender: string;
  height: string;
  mass: string;
  hairColor: string;
  skinColor: string;
  eyeColor: string;
  homeworldId: number | null;
  homeworld: string | null;
  filmIds: number[];
  films: string[];
  speciesIds: number[];
  species: string[];
  vehicleIds: number[];
  vehicles: string[];
  starshipIds: number[];
  starships: string[];
  created: Date | null;
  edited: Date | null;
  url: string;
}
