package com.notebook.api.ai.application;

public class UnsupportedAssistantActionApplicationException extends RuntimeException {

	private final String action;

	public UnsupportedAssistantActionApplicationException(String action) {
		super("Unsupported assistant action application: " + action);
		this.action = action;
	}

	public String action() {
		return this.action;
	}
}
