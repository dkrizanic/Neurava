package com.notebook.api.auth.application;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.notebook.api.auth.application.CurrentSession.AccountSummary;
import com.notebook.api.auth.domain.AuthAccount;

@Service
public class CurrentSessionService {

	private final AuthAccountService accountService;

	public CurrentSessionService(AuthAccountService accountService) {
		this.accountService = accountService;
	}

	public CurrentSession currentSession(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return CurrentSession.anonymous();
		}

		if (authentication.getPrincipal() instanceof OAuth2User) {
			AuthAccount account = this.accountService.createOrUpdateFrom(authentication);
			return CurrentSession.authenticated(new AccountSummary(
					account.getId(),
					account.getEmail(),
					account.getDisplayName(),
					account.getAvatarUrl()));
		}

		return CurrentSession.authenticated(new AccountSummary(null, null, authentication.getName(), null));
	}
}
