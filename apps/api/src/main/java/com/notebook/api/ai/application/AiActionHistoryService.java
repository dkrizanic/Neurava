package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionPreviewNames.CREATE_NOTE;
import static com.notebook.api.ai.application.AssistantActionPreviewNames.FIX_NOTE_GRAMMAR;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.notebook.api.ai.domain.AiActionHistoryRecord;
import com.notebook.api.ai.infrastructure.persistence.AiActionHistoryRepository;
import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteSummary;

@Service
public class AiActionHistoryService {

	private final AiActionHistoryRepository history;
	private final NoteService notes;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AiActionHistoryService(AiActionHistoryRepository history, NoteService notes) {
		this.history = history;
		this.notes = notes;
	}

	@Transactional
	public AiActionHistorySummary recordCreatedNote(UUID ownerAccountId, UUID workspaceContextId, NoteSummary note,
			String summary) {
		AiActionHistoryRecord record = AiActionHistoryRecord.create(
				ownerAccountId,
				workspaceContextId,
				CREATE_NOTE,
				"note",
				note.id(),
				"create",
				summary,
				null,
				noteState(note),
				Instant.now());
		return AiActionHistorySummary.from(this.history.save(record));
	}

	@Transactional
	public AiActionHistorySummary recordUpdatedNote(UUID ownerAccountId, UUID workspaceContextId, NoteSummary previous,
			NoteSummary current, String action, String summary) {
		AiActionHistoryRecord record = AiActionHistoryRecord.create(
				ownerAccountId,
				workspaceContextId,
				action,
				"note",
				current.id(),
				"update",
				summary,
				noteState(previous),
				noteState(current),
				Instant.now());
		return AiActionHistorySummary.from(this.history.save(record));
	}

	@Transactional(readOnly = true)
	public List<AiActionHistorySummary> list(UUID workspaceContextId) {
		return this.history.findByWorkspaceContextIdOrderByCreatedAtDesc(workspaceContextId).stream()
				.map(AiActionHistorySummary::from)
				.toList();
	}

	@Transactional
	public AiActionHistorySummary revert(UUID historyId, UUID workspaceContextId) {
		AiActionHistoryRecord record = this.history.findByIdAndWorkspaceContextId(historyId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI action history record not found."));

		if (record.isReverted()) {
			return AiActionHistorySummary.from(record);
		}
		if (!CREATE_NOTE.equals(record.getAction()) || !"note".equals(record.getEntityType())
				|| !"create".equals(record.getChangeType())) {
			if (FIX_NOTE_GRAMMAR.equals(record.getAction()) && "note".equals(record.getEntityType())
					&& "update".equals(record.getChangeType())) {
				return revertGrammarFix(record, workspaceContextId);
			}
			throw new AiActionRevertException("This AI action cannot be reverted yet.");
		}

		try {
			this.notes.delete(record.getEntityId(), workspaceContextId);
		} catch (ResponseStatusException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new AiActionRevertException("The note created by this AI action no longer exists.");
			}
			throw exception;
		}

		record.markReverted("Removed AI-created note.", Instant.now());
		return AiActionHistorySummary.from(record);
	}

	private AiActionHistorySummary revertGrammarFix(AiActionHistoryRecord record, UUID workspaceContextId) {
		try {
			var previous = this.objectMapper.readTree(record.getPreviousState());
			String title = previous.path("title").asText("");
			String body = previous.path("body").asText("");
			if (title.isBlank()) {
				throw new AiActionRevertException("The previous note state is no longer available.");
			}
			this.notes.update(record.getEntityId(), workspaceContextId, title, body);
			record.markReverted("Restored note text before the grammar fix.", Instant.now());
			return AiActionHistorySummary.from(record);
		} catch (JsonProcessingException exception) {
			throw new AiActionRevertException("The previous note state is no longer available.");
		} catch (ResponseStatusException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new AiActionRevertException("The note changed by this AI action no longer exists.");
			}
			throw exception;
		}
	}

	private static String noteState(NoteSummary note) {
		return """
				{"id":"%s","title":"%s","body":"%s","noteDate":"%s","tags":"%s","favorite":%s,"pinned":%s,"archivedAt":%s,"editorMode":"%s","linkedResources":"%s"}
				""".formatted(
				note.id(),
				escape(note.title()),
				escape(note.body()),
				note.noteDate(),
				escape(note.tags()),
				note.favorite(),
				note.pinned(),
				note.archivedAt() == null ? "null" : "\"" + note.archivedAt() + "\"",
				escape(note.editorMode()),
				escape(note.linkedResources())).strip();
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n");
	}
}
