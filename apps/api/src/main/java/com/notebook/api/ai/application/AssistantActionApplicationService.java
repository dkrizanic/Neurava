package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionPreviewNames.CREATE_NOTE;
import static com.notebook.api.ai.application.AssistantActionPreviewNames.FIX_NOTE_GRAMMAR;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteSummary;

@Service
public class AssistantActionApplicationService {

	private final NoteService notes;
	private final AiActionHistoryService history;
	private final Validator validator;

	public AssistantActionApplicationService(NoteService notes, AiActionHistoryService history, Validator validator) {
		this.notes = notes;
		this.history = history;
		this.validator = validator;
	}

	public AssistantActionApplicationResponse apply(UUID ownerAccountId, UUID workspaceContextId,
			AssistantActionApplicationRequest request) {
		return switch (request.action()) {
			case CREATE_NOTE -> applyCreateNote(ownerAccountId, workspaceContextId, request.input());
			case FIX_NOTE_GRAMMAR -> applyGrammarFix(ownerAccountId, workspaceContextId, request.input());
			default -> throw new UnsupportedAssistantActionApplicationException(request.action());
		};
	}

	private AssistantActionApplicationResponse applyCreateNote(UUID ownerAccountId, UUID workspaceContextId,
			Map<String, Object> inputNode) {
		CreateNoteApplyInput input = validateInput(new CreateNoteApplyInput(
				textField(inputNode, "title"),
				nullableTextField(inputNode, "body"),
				nullableTextField(inputNode, "tags"),
				nullableTextField(inputNode, "linkedResources"),
				dateField(inputNode, "noteDate")));

		NoteSummary entity = this.notes.create(ownerAccountId, workspaceContextId, input.title(), input.body(),
				input.noteDate(), input.tags(), input.linkedResources());
		String summary = "Created note \"%s\".".formatted(entity.title());
		this.history.recordCreatedNote(ownerAccountId, workspaceContextId, entity, summary);

		return new AssistantActionApplicationResponse(
				CREATE_NOTE,
				"note",
				"create",
				summary,
				entity);
	}

	private AssistantActionApplicationResponse applyGrammarFix(UUID ownerAccountId, UUID workspaceContextId,
			Map<String, Object> inputNode) {
		GrammarFixApplyInput input = validateInput(new GrammarFixApplyInput(
				textField(inputNode, "noteId"),
				textField(inputNode, "title"),
				nullableTextField(inputNode, "body"),
				textField(inputNode, "proposedBody")));
		UUID noteId = uuidField(input.noteId(), "noteId");
		NoteSummary existing = this.notes.get(noteId, workspaceContextId);
		NoteSummary previous = new NoteSummary(
				existing.id(),
				existing.ownerAccountId(),
				existing.workspaceContextId(),
				input.title(),
				input.body(),
				existing.createdAt(),
				existing.updatedAt(),
				existing.noteDate(),
				existing.archivedAt(),
				existing.tags(),
				existing.favorite(),
				existing.pinned(),
				existing.editorMode(),
				existing.linkedResources());
		NoteSummary entity = this.notes.update(noteId, workspaceContextId, input.title(), input.proposedBody());
		String summary = "Applied grammar fix to \"%s\".".formatted(entity.title());
		this.history.recordUpdatedNote(ownerAccountId, workspaceContextId, previous, entity, FIX_NOTE_GRAMMAR, summary);

		return new AssistantActionApplicationResponse(
				FIX_NOTE_GRAMMAR,
				"note",
				"update",
				summary,
				entity);
	}

	private <T> T validateInput(T input) {
		List<AssistantActionInputError> errors = this.validator.validate(input).stream()
				.map(AssistantActionApplicationService::toError)
				.toList();
		if (!errors.isEmpty()) {
			throw new InvalidAssistantActionInputException(errors);
		}
		return input;
	}

	private static String textField(Map<String, Object> inputNode, String fieldName) {
		Object field = inputNode.get(fieldName);
		if (field == null) {
			return null;
		}
		if (!(field instanceof String value)) {
			throw new InvalidAssistantActionInputException(
					List.of(new AssistantActionInputError(fieldName, "Value must be text.")));
		}
		return value;
	}

	private static String nullableTextField(Map<String, Object> inputNode, String fieldName) {
		String value = textField(inputNode, fieldName);
		return value == null ? "" : value;
	}

	private static java.time.LocalDate dateField(Map<String, Object> inputNode, String fieldName) {
		Object field = inputNode.get(fieldName);
		if (field == null) {
			return null;
		}
		if (!(field instanceof String value)) {
			throw new InvalidAssistantActionInputException(
					List.of(new AssistantActionInputError(fieldName, "Value must be an ISO date.")));
		}
		try {
			return java.time.LocalDate.parse(value);
		} catch (java.time.format.DateTimeParseException exception) {
			throw new InvalidAssistantActionInputException(
					List.of(new AssistantActionInputError(fieldName, "Value must be an ISO date.")));
		}
	}

	private static UUID uuidField(String value, String fieldName) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			throw new InvalidAssistantActionInputException(
					List.of(new AssistantActionInputError(fieldName, "Value must be a valid id.")));
		}
	}

	private static AssistantActionInputError toError(ConstraintViolation<?> violation) {
		return new AssistantActionInputError(violation.getPropertyPath().toString(), violation.getMessage());
	}
}
