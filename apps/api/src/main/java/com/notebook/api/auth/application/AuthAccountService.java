package com.notebook.api.auth.application;

import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.auth.domain.AuthAccount;
import com.notebook.api.auth.infrastructure.persistence.AuthAccountRepository;

@Service
public class AuthAccountService {

	private static final String GOOGLE_PROVIDER = "google";

	private final AuthAccountRepository accounts;

	public AuthAccountService(AuthAccountRepository accounts) {
		this.accounts = accounts;
	}

	@Transactional
	public AuthAccount createOrUpdateFrom(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauthUser)) {
			throw new IllegalArgumentException("Authentication does not contain an OAuth2 principal.");
		}

		String subject = subjectFor(oauthUser);
		if (subject == null || subject.isBlank()) {
			throw new IllegalArgumentException("OAuth2 principal is missing a stable subject.");
		}

		Instant now = Instant.now();
		AuthAccount account = this.accounts.findByProviderAndProviderSubject(GOOGLE_PROVIDER, subject)
				.orElseGet(() -> AuthAccount.create(GOOGLE_PROVIDER, subject, now));
		account.updateProfile(attribute(oauthUser, "email"), attribute(oauthUser, "name"), attribute(oauthUser, "picture"), now);
		return this.accounts.save(account);
	}

	private static String subjectFor(OAuth2User user) {
		if (user instanceof OidcUser oidcUser) {
			return oidcUser.getSubject();
		}
		return user.getName();
	}

	private static String attribute(OAuth2User user, String name) {
		Object value = user.getAttribute(name);
		return value instanceof String stringValue && !stringValue.isBlank() ? stringValue : null;
	}
}
