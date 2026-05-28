package com.notebook.api.auth.application;

import java.util.UUID;

public record CurrentSession(
		boolean authenticated,
		AccountSummary account
) {

	public static CurrentSession anonymous() {
		return new CurrentSession(false, null);
	}

	public static CurrentSession authenticated(AccountSummary account) {
		return new CurrentSession(true, account);
	}

	public record AccountSummary(
			UUID id,
			String email,
			String displayName,
			String avatarUrl
	) {
	}
}
