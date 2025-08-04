export interface Character {
  id: number;
  name: string;
  birthYear: string;
  gender: string;
  height: string;
  mass: string;
  hairColor: string;
  skinColor: string;
  eyeColor: string;
  homeworld: string | null;
  homeworldId: number | null;
  films: string[];
  filmIds: number[];
  species: string[];
  speciesIds: number[];
  vehicles: string[];
  vehicleIds: number[];
  starships: string[];
  starshipIds: number[];
  created: Date;
  edited: Date;
  url: string;
}
