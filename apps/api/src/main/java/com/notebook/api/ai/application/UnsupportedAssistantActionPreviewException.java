package com.notebook.api.ai.application;

public class UnsupportedAssistantActionPreviewException extends RuntimeException {

	private final String action;

	public UnsupportedAssistantActionPreviewException(String action) {
		super("Unsupported assistant action preview: " + action);
		this.action = action;
	}

	public String action() {
		return this.action;
	}
}
