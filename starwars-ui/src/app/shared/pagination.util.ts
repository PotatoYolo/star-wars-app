export function buildPagesArray(
  current: number,
  totalPages: number,
  maxFull = 4
): number[] {

  const last = totalPages - 1;

  if (totalPages <= maxFull) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  const arr: number[] = [0];

  if (current > 2) arr.push(-1);

  const start = Math.max(1, current - 1);
  const end   = Math.min(last - 1, current + 1);

  for (let i = start; i <= end; i++) arr.push(i);

  if (current < last - 2) arr.push(-1);

  arr.push(last);
  return arr;
}
