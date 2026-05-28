import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { AuthSession } from '../types';

export async function fetchCurrentSession(signal?: AbortSignal): Promise<AuthSession> {
  const response = await fetch(`${apiBaseUrl}/api/v1/auth/session`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to load session');
  }

  return response.json() as Promise<AuthSession>;
}

export function startGoogleSignIn() {
  window.location.assign(`${apiBaseUrl}/oauth2/authorization/google`);
}

export async function logout() {
  await fetch(`${apiBaseUrl}/api/v1/auth/logout`, {
    credentials: 'include',
    method: 'POST',
  });
}
