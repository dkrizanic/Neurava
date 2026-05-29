package com.notebook.api.auth.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.AccountAuthenticatedEvent;
import com.notebook.api.auth.domain.AuthAccount;
import com.notebook.api.auth.infrastructure.persistence.AuthAccountRepository;

class AuthAccountServiceUnitTest {

	private final AuthAccountRepository accounts = mock(AuthAccountRepository.class);
	private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
	private final AuthAccountService service = new AuthAccountService(this.accounts, this.events);

	@Test
	void createsGoogleAccountFromOauthPrincipal() {
		Authentication authentication = mock(Authentication.class);
		OAuth2User user = mock(OAuth2User.class);
		when(authentication.getPrincipal()).thenReturn(user);
		when(user.getName()).thenReturn("google-subject");
		when(user.getAttribute("email")).thenReturn("dario@example.com");
		when(user.getAttribute("name")).thenReturn("Dario Notebook");
		when(user.getAttribute("picture")).thenReturn("https://example.com/avatar.png");
		when(this.accounts.findByProviderAndProviderSubject("google", "google-subject")).thenReturn(Optional.empty());
		when(this.accounts.save(any(AuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AuthAccount account = this.service.createOrUpdateFrom(authentication);

		assertThat(account.getProvider()).isEqualTo("google");
		assertThat(account.getProviderSubject()).isEqualTo("google-subject");
		assertThat(account.getEmail()).isEqualTo("dario@example.com");
		assertThat(account.getDisplayName()).isEqualTo("Dario Notebook");
		assertThat(account.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
		verify(this.accounts).save(account);
		ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
		verify(this.events).publishEvent(event.capture());
		assertThat(event.getValue()).isInstanceOf(AccountAuthenticatedEvent.class);
		assertThat(((AccountAuthenticatedEvent) event.getValue()).accountId()).isEqualTo(account.getId());
	}

	@Test
	void updatesExistingGoogleAccountProfile() {
		Authentication authentication = mock(Authentication.class);
		OAuth2User user = mock(OAuth2User.class);
		AuthAccount existing = AuthAccount.create("google", "google-subject", java.time.Instant.now());
		when(authentication.getPrincipal()).thenReturn(user);
		when(user.getName()).thenReturn("google-subject");
		when(user.getAttribute("email")).thenReturn("updated@example.com");
		when(user.getAttribute("name")).thenReturn("Updated User");
		when(this.accounts.findByProviderAndProviderSubject("google", "google-subject")).thenReturn(Optional.of(existing));
		when(this.accounts.save(any(AuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AuthAccount account = this.service.createOrUpdateFrom(authentication);

		assertThat(account).isSameAs(existing);
		assertThat(account.getEmail()).isEqualTo("updated@example.com");
		assertThat(account.getDisplayName()).isEqualTo("Updated User");
		ArgumentCaptor<AuthAccount> saved = ArgumentCaptor.forClass(AuthAccount.class);
		verify(this.accounts).save(saved.capture());
		assertThat(saved.getValue()).isSameAs(existing);
	}

	@Test
	void rejectsAuthenticationWithoutOauthPrincipal() {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn("not-oauth");

		assertThatThrownBy(() -> this.service.createOrUpdateFrom(authentication))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("OAuth2 principal");
	}
}
