package com.notebook.api.workspace.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.notebook.api.auth.application.AccountAuthenticatedEvent;

@Component
public class AccountWorkspaceInitializer {

	private final PersonalWorkspaceService personalWorkspaces;

	public AccountWorkspaceInitializer(PersonalWorkspaceService personalWorkspaces) {
		this.personalWorkspaces = personalWorkspaces;
	}

	@EventListener
	public void on(AccountAuthenticatedEvent event) {
		this.personalWorkspaces.ensurePersonalWorkspace(event.accountId());
	}
}
