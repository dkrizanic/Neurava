package com.notebook.api.auth.application;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.notebook.api.auth.application.CurrentSession.AccountSummary;
import com.notebook.api.auth.domain.AuthAccount;
import com.notebook.api.workspace.application.PersonalWorkspaceService;
import com.notebook.api.workspace.application.WorkspaceSession;

@Service
public class CurrentSessionService {

	private final AuthAccountService accountService;
	private final PersonalWorkspaceService personalWorkspaces;

	public CurrentSessionService(AuthAccountService accountService, PersonalWorkspaceService personalWorkspaces) {
		this.accountService = accountService;
		this.personalWorkspaces = personalWorkspaces;
	}

	public CurrentSession currentSession(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return CurrentSession.anonymous();
		}

		if (authentication.getPrincipal() instanceof OAuth2User) {
			AuthAccount account = this.accountService.createOrUpdateFrom(authentication);
			WorkspaceSession workspace = this.personalWorkspaces.ensurePersonalWorkspace(account);
			return CurrentSession.authenticated(
					new AccountSummary(
							account.getId(),
							account.getEmail(),
							account.getDisplayName(),
							account.getAvatarUrl()),
					workspace.activeWorkspace(),
					workspace.workspaceSwitcherAvailable());
		}

		return CurrentSession.authenticated(new AccountSummary(null, null, authentication.getName(), null), null, false);
	}
}
