package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionPreviewNames.CREATE_NOTE;
import static com.notebook.api.ai.application.AssistantActionPreviewNames.FIX_NOTE_GRAMMAR;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

@Service
public class AssistantActionPreviewService {

	private static final Set<String> STOP_WORDS = Set.of(
			"about", "after", "again", "also", "because", "before", "could", "from", "have", "into",
			"just", "need", "notes", "should", "that", "their", "there", "this", "with", "would");

	private final Validator validator;

	public AssistantActionPreviewService(Validator validator) {
		this.validator = validator;
	}

	public AssistantActionPreviewResponse preview(UUID workspaceContextId, AssistantActionPreviewRequest request) {
		return switch (request.action()) {
			case CREATE_NOTE -> createNotePreview(request.input());
			case FIX_NOTE_GRAMMAR -> grammarFixPreview(request.input());
			default -> throw new UnsupportedAssistantActionPreviewException(request.action());
		};
	}

	private AssistantActionPreviewResponse createNotePreview(Map<String, Object> inputNode) {
		CreateNotePreviewInput input = validateInput(new CreateNotePreviewInput(textField(inputNode, "text")));
		NoteChangePreview preview = new NoteChangePreview(
				titleFrom(input.text()),
				bodyFrom(input.text()),
				tagsFrom(input.text()));
		return new AssistantActionPreviewResponse(
				CREATE_NOTE,
				"note",
				"create",
				"Create a new note draft in the active workspace.",
				preview);
	}

	private AssistantActionPreviewResponse grammarFixPreview(Map<String, Object> inputNode) {
		GrammarFixPreviewInput input = validateInput(new GrammarFixPreviewInput(
				textField(inputNode, "noteId"),
				nullableTextField(inputNode, "title"),
				textField(inputNode, "body")));
		GrammarFixPreview preview = new GrammarFixPreview(
				input.noteId(),
				input.body(),
				grammarFixed(input.body()));
		return new AssistantActionPreviewResponse(
				FIX_NOTE_GRAMMAR,
				"note",
				"update",
				"Preview grammar fixes for \"%s\".".formatted(input.title() == null || input.title().isBlank()
						? "this note" : input.title().trim()),
				preview);
	}

	private <T> T validateInput(T input) {
		List<AssistantActionInputError> errors = this.validator.validate(input).stream()
				.map(AssistantActionPreviewService::toError)
				.toList();
		if (!errors.isEmpty()) {
			throw new InvalidAssistantActionInputException(errors);
		}
		return input;
	}

	private static String titleFrom(String text) {
		String firstLine = Arrays.stream(text.strip().split("\\R"))
				.map(String::strip)
				.filter(line -> !line.isBlank())
				.findFirst()
				.orElse("Untitled AI note");
		String title = firstLine.replaceAll("\\s+", " ");
		if (title.length() <= 80) {
			return title;
		}
		return title.substring(0, 77).stripTrailing() + "...";
	}

	private static String bodyFrom(String text) {
		return text.strip().replaceAll("\\R{3,}", "\n\n");
	}

	private static String tagsFrom(String text) {
		LinkedHashSet<String> tags = new LinkedHashSet<>();
		for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
			if (token.length() >= 4 && !STOP_WORDS.contains(token)) {
				tags.add(token);
			}
			if (tags.size() == 3) {
				break;
			}
		}
		return String.join(",", tags);
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

	private static String grammarFixed(String text) {
		String normalized = text.strip()
				.replaceAll("[ \\t]+", " ")
				.replaceAll("\\s+([,.!?;:])", "$1")
				.replaceAll("([,.!?;:])([^\\s\\n])", "$1 $2")
				.replaceAll("(?i)\\bteh\\b", "the")
				.replaceAll("(?i)\\bdont\\b", "don't")
				.replaceAll("(?i)\\bcant\\b", "can't")
				.replaceAll("(?i)\\bwont\\b", "won't")
				.replaceAll("(?i)\\brecieve\\b", "receive")
				.replaceAll("(?i)\\bi\\b", "I");
		StringBuilder corrected = new StringBuilder(normalized.length());
		boolean capitalizeNext = true;
		for (int index = 0; index < normalized.length(); index++) {
			char character = normalized.charAt(index);
			if (capitalizeNext && Character.isLetter(character)) {
				corrected.append(Character.toUpperCase(character));
				capitalizeNext = false;
				continue;
			}
			corrected.append(character);
			if (character == '.' || character == '!' || character == '?' || character == '\n') {
				capitalizeNext = true;
			}
		}
		return corrected.toString();
	}

	private static AssistantActionInputError toError(ConstraintViolation<?> violation) {
		return new AssistantActionInputError(violation.getPropertyPath().toString(), violation.getMessage());
	}
}
