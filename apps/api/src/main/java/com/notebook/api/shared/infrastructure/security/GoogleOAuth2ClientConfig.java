package com.notebook.api.shared.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
class GoogleOAuth2ClientConfig {

	@Bean
	@Conditional(GoogleOAuth2CredentialsCondition.class)
	ClientRegistrationRepository googleClientRegistrationRepository(Environment environment) {
		ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
				.clientId(requiredProperty(environment, "GOOGLE_CLIENT_ID"))
				.clientSecret(requiredProperty(environment, "GOOGLE_CLIENT_SECRET"))
				.build();

		return new InMemoryClientRegistrationRepository(google);
	}

	private static String requiredProperty(Environment environment, String name) {
		String value = environment.getProperty(name);
		if (!StringUtils.hasText(value)) {
			throw new IllegalStateException(name + " must be configured for Google OAuth login.");
		}
		return value;
	}

	static class GoogleOAuth2CredentialsCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			Environment environment = context.getEnvironment();
			return StringUtils.hasText(environment.getProperty("GOOGLE_CLIENT_ID"))
					&& StringUtils.hasText(environment.getProperty("GOOGLE_CLIENT_SECRET"));
		}
	}
}
