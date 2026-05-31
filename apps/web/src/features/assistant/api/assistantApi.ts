import { apiBaseUrl } from '../../../shared/api/httpClient';
import type {
  AssistantActionName,
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
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-previews`, {
    body: JSON.stringify({ action: 'create_note', input: { text } }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to preview note change');
  }

  return response.json() as Promise<AssistantActionPreviewResponse>;
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
    throw new Error(`Unable to execute assistant action: ${action}`);
  }

  const actionResponse = await response.json() as AssistantActionResponse<TAction>;
  return actionResponse.result;
}
