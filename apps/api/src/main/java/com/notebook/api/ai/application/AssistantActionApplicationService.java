package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionPreviewNames.CREATE_NOTE;

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
	private final Validator validator;

	public AssistantActionApplicationService(NoteService notes, Validator validator) {
		this.notes = notes;
		this.validator = validator;
	}

	public AssistantActionApplicationResponse apply(UUID ownerAccountId, UUID workspaceContextId,
			AssistantActionApplicationRequest request) {
		return switch (request.action()) {
			case CREATE_NOTE -> applyCreateNote(ownerAccountId, workspaceContextId, request.input());
			default -> throw new UnsupportedAssistantActionApplicationException(request.action());
		};
	}

	private AssistantActionApplicationResponse applyCreateNote(UUID ownerAccountId, UUID workspaceContextId,
			Map<String, Object> inputNode) {
		CreateNoteApplyInput input = validateInput(new CreateNoteApplyInput(
				textField(inputNode, "title"),
				nullableTextField(inputNode, "body"),
				nullableTextField(inputNode, "tags"),
				dateField(inputNode, "noteDate")));

		NoteSummary entity = this.notes.create(ownerAccountId, workspaceContextId, input.title(), input.body(),
				input.noteDate(), input.tags());

		return new AssistantActionApplicationResponse(
				CREATE_NOTE,
				"note",
				"create",
				"Created note \"%s\".".formatted(entity.title()),
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

	private static AssistantActionInputError toError(ConstraintViolation<?> violation) {
		return new AssistantActionInputError(violation.getPropertyPath().toString(), violation.getMessage());
	}
}
