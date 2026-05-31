package com.notebook.api.ai.application;

import static com.notebook.api.ai.application.AssistantActionNames.ANSWER_QUESTION;
import static com.notebook.api.ai.application.AssistantActionNames.SEARCH_MEMORY;
import static com.notebook.api.ai.application.AssistantActionNames.SUMMARIZE_HISTORY;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;

@Service
public class AssistantActionService {

	private final SourceAwareAnswerService answers;
	private final HistorySummaryService summaries;
	private final MemorySearchService search;
	private final Validator validator;

	public AssistantActionService(SourceAwareAnswerService answers, HistorySummaryService summaries,
			MemorySearchService search, Validator validator) {
		this.answers = answers;
		this.summaries = summaries;
		this.search = search;
		this.validator = validator;
	}

	public AssistantActionResponse execute(UUID workspaceContextId, AssistantActionRequest request) {
		return switch (request.action()) {
			case ANSWER_QUESTION -> answerQuestion(workspaceContextId, request.input());
			case SEARCH_MEMORY -> searchMemory(workspaceContextId, request.input());
			case SUMMARIZE_HISTORY -> summarizeHistory(workspaceContextId, request.input());
			default -> throw new UnsupportedAssistantActionException(request.action());
		};
	}

	private AssistantActionResponse answerQuestion(UUID workspaceContextId, Map<String, Object> inputNode) {
		AnswerQuestionActionInput input = validateInput(new AnswerQuestionActionInput(textField(inputNode, "question")));
		return new AssistantActionResponse(ANSWER_QUESTION, this.answers.answer(workspaceContextId, input.question()));
	}

	private AssistantActionResponse searchMemory(UUID workspaceContextId, Map<String, Object> inputNode) {
		SearchMemoryActionInput input = validateInput(new SearchMemoryActionInput(textField(inputNode, "query")));
		return new AssistantActionResponse(SEARCH_MEMORY, this.search.searchNotes(workspaceContextId, input.query()));
	}

	private AssistantActionResponse summarizeHistory(UUID workspaceContextId, Map<String, Object> inputNode) {
		SummarizeHistoryActionInput input = validateInput(new SummarizeHistoryActionInput(textField(inputNode, "topic")));
		return new AssistantActionResponse(SUMMARIZE_HISTORY, this.summaries.summarize(workspaceContextId, input.topic()));
	}

	private <T> T validateInput(T input) {
		List<AssistantActionInputError> errors = this.validator.validate(input).stream()
				.map(AssistantActionService::toError)
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

	private static AssistantActionInputError toError(ConstraintViolation<?> violation) {
		return new AssistantActionInputError(violation.getPropertyPath().toString(), violation.getMessage());
	}
}
