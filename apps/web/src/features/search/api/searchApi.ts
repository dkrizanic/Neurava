import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { MemorySearchMatch } from '../types';

export async function searchMemory(query: string, signal?: AbortSignal): Promise<MemorySearchMatch[]> {
  const params = new URLSearchParams({ q: query });
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/search?${params.toString()}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to search memory');
  }

  return response.json() as Promise<MemorySearchMatch[]>;
}
