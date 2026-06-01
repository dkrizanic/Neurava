package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionPreviewNames.CREATE_NOTE;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.ai.domain.AiActionHistoryRecord;
import com.notebook.api.ai.infrastructure.persistence.AiActionHistoryRepository;
import com.notebook.api.notes.application.NoteSummary;

@Service
public class AiActionHistoryService {

	private final AiActionHistoryRepository history;

	public AiActionHistoryService(AiActionHistoryRepository history) {
		this.history = history;
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

	@Transactional(readOnly = true)
	public List<AiActionHistorySummary> list(UUID workspaceContextId) {
		return this.history.findByWorkspaceContextIdOrderByCreatedAtDesc(workspaceContextId).stream()
				.map(AiActionHistorySummary::from)
				.toList();
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
