package com.notebook.api.ai.application;

import java.util.List;

public class InvalidAssistantActionInputException extends RuntimeException {

	private final List<AssistantActionInputError> errors;

	public InvalidAssistantActionInputException(List<AssistantActionInputError> errors) {
		super("Assistant action input validation failed.");
		this.errors = List.copyOf(errors);
	}

	public List<AssistantActionInputError> errors() {
		return this.errors;
	}
}
