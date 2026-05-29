import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { CompanyRegistration } from '../types';

export async function registerCompany(name: string): Promise<CompanyRegistration> {
  const response = await fetch(`${apiBaseUrl}/api/v1/workspaces/companies`, {
    body: JSON.stringify({ name }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  if (!response.ok) {
    throw new Error('Unable to register company');
  }

  return response.json() as Promise<CompanyRegistration>;
}
