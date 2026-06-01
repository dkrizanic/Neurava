import { apiBaseUrl } from '../../../shared/api/httpClient';
import type {
  AiActionHistorySummary,
  AssistantActionName,
  AssistantActionApplicationResponse,
  AssistantActionPreviewResponse,
  AssistantActionResponse,
  AssistantActionResultByName,
  HistorySummary,
  SourceAwareAnswer,
} from '../types';

export async function answerQuestion(question: string, signal?: AbortSignal): Promise<SourceAwareAnswer> {
  return executeAssistantAction('answer_question', { question }, signal);
}

export async function summarizeHistory(topic: string, signal?: AbortSignal): Promise<HistorySummary> {
  return executeAssistantAction('summarize_history', { topic }, signal);
}

export async function searchMemoryAction(query: string, signal?: AbortSignal) {
  return executeAssistantAction('search_memory', { query }, signal);
}

export async function previewCreateNote(text: string, signal?: AbortSignal): Promise<AssistantActionPreviewResponse> {
  return previewAction('create_note', { text }, signal);
}

export async function previewCreateReminder(text: string, signal?: AbortSignal): Promise<AssistantActionPreviewResponse> {
  return previewAction('create_reminder', { text }, signal);
}

export async function previewCreatePlan(goal: string, signal?: AbortSignal): Promise<AssistantActionPreviewResponse> {
  return previewAction('create_plan', { goal }, signal);
}

export async function applyCreateNotePreview(
  preview: { body: string; linkedResources?: string; noteDate?: string; tags: string; title: string },
  signal?: AbortSignal,
): Promise<AssistantActionApplicationResponse> {
  return applyPreview('create_note', preview, signal);
}

export async function applyReminderPreview(
  preview: { calendarSyncEnabled: boolean; details: string; dueAt: string; relatedContext: string; title: string },
  signal?: AbortSignal,
): Promise<AssistantActionApplicationResponse> {
  return applyPreview('create_reminder', preview, signal);
}

export async function applyPlanPreview(
  preview: { goal: string; items: string; linkedResources: string; title: string },
  signal?: AbortSignal,
): Promise<AssistantActionApplicationResponse> {
  return applyPreview('create_plan', preview, signal);
}

export async function fetchAiActionHistory(signal?: AbortSignal): Promise<AiActionHistorySummary[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-history`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    signal,
  });

  if (!response.ok) {
    throwAssistantApiError(response, 'Unable to load AI action history');
  }

  return response.json() as Promise<AiActionHistorySummary[]>;
}

export async function revertAiAction(historyId: string, signal?: AbortSignal): Promise<AiActionHistorySummary> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-history/${historyId}/revert`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throwAssistantApiError(response, 'Unable to revert AI action');
  }

  return response.json() as Promise<AiActionHistorySummary>;
}

async function executeAssistantAction<TAction extends AssistantActionName>(
  action: TAction,
  input: Record<string, string>,
  signal?: AbortSignal,
): Promise<AssistantActionResultByName[TAction]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/actions`, {
    body: JSON.stringify({ action, input }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throwAssistantApiError(response, `Unable to execute assistant action: ${action}`);
  }

  const actionResponse = await response.json() as AssistantActionResponse<TAction>;
  return actionResponse.result;
}

async function previewAction(
  action: string,
  input: Record<string, unknown>,
  signal?: AbortSignal,
): Promise<AssistantActionPreviewResponse> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-previews`, {
    body: JSON.stringify({ action, input }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throwAssistantApiError(response, 'Unable to preview AI change');
  }

  return response.json() as Promise<AssistantActionPreviewResponse>;
}

async function applyPreview(
  action: string,
  input: Record<string, unknown>,
  signal?: AbortSignal,
): Promise<AssistantActionApplicationResponse> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-applications`, {
    body: JSON.stringify({ action, input }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throwAssistantApiError(response, 'Unable to apply AI change');
  }

  return response.json() as Promise<AssistantActionApplicationResponse>;
}

function throwAssistantApiError(response: Response, fallbackMessage: string): never {
  if (response.status === 401 || response.redirected) {
    throw new Error('AUTH_REQUIRED');
  }
  throw new Error(fallbackMessage);
}
