package com.notebook.api.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.auth.domain.AuthAccount;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, UUID> {

	Optional<AuthAccount> findByProviderAndProviderSubject(String provider, String providerSubject);
}
