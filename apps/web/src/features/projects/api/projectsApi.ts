import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Project, ProjectHistorySummary, UpsertProjectInput } from '../types';

export async function fetchProjects(signal?: AbortSignal): Promise<Project[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/projects`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to load projects');
  }
  return response.json() as Promise<Project[]>;
}

export async function createProject(input: UpsertProjectInput): Promise<Project> {
  const response = await fetch(`${apiBaseUrl}/api/v1/projects`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error('Unable to create project');
  }
  return response.json() as Promise<Project>;
}

export async function summarizeProject(projectId: string, signal?: AbortSignal): Promise<ProjectHistorySummary> {
  const response = await fetch(`${apiBaseUrl}/api/v1/projects/${projectId}/summary`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to summarize project');
  }
  return response.json() as Promise<ProjectHistorySummary>;
}

