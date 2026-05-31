package com.notebook.api.ai.application;

public class UnsupportedAssistantActionException extends RuntimeException {

	private final String action;

	public UnsupportedAssistantActionException(String action) {
		super("Unsupported assistant action: " + action);
		this.action = action;
	}

	public String action() {
		return this.action;
	}
}
