package com.notebook.api.auth.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "auth_account", uniqueConstraints = {
		@UniqueConstraint(name = "uk_auth_account_provider_subject", columnNames = { "provider", "provider_subject" })
})
public class AuthAccount {

	@Id
	private UUID id;

	@Column(nullable = false, length = 40)
	private String provider;

	@Column(name = "provider_subject", nullable = false)
	private String providerSubject;

	@Column(length = 320)
	private String email;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "avatar_url", length = 1024)
	private String avatarUrl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected AuthAccount() {
	}

	private AuthAccount(String provider, String providerSubject, Instant now) {
		this.id = UUID.randomUUID();
		this.provider = provider;
		this.providerSubject = providerSubject;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static AuthAccount create(String provider, String providerSubject, Instant now) {
		return new AuthAccount(provider, providerSubject, now);
	}

	public UUID getId() {
		return this.id;
	}

	public String getProvider() {
		return this.provider;
	}

	public String getProviderSubject() {
		return this.providerSubject;
	}

	public String getEmail() {
		return this.email;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getAvatarUrl() {
		return this.avatarUrl;
	}

	public void updateProfile(String email, String displayName, String avatarUrl, Instant now) {
		this.email = email;
		this.displayName = displayName;
		this.avatarUrl = avatarUrl;
		this.updatedAt = now;
	}
}
