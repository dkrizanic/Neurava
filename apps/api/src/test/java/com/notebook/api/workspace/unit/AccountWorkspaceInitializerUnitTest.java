package com.notebook.api.workspace.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.notebook.api.auth.application.AccountAuthenticatedEvent;
import com.notebook.api.workspace.application.AccountWorkspaceInitializer;
import com.notebook.api.workspace.application.PersonalWorkspaceService;

class AccountWorkspaceInitializerUnitTest {

	private final PersonalWorkspaceService personalWorkspaces = mock(PersonalWorkspaceService.class);
	private final AccountWorkspaceInitializer initializer = new AccountWorkspaceInitializer(this.personalWorkspaces);

	@Test
	void accountAuthenticatedEventEnsuresPersonalWorkspace() {
		UUID accountId = UUID.randomUUID();

		this.initializer.on(new AccountAuthenticatedEvent(accountId, "dario@example.com", "Dario Notebook"));

		verify(this.personalWorkspaces).ensurePersonalWorkspace(accountId);
	}
}
