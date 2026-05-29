package com.notebook.api.auth.application;

import java.util.UUID;

public record AccountAuthenticatedEvent(
		UUID accountId,
		String email,
		String displayName
) {
}
