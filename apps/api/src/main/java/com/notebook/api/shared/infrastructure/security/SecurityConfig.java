package com.notebook.api.shared.infrastructure.security;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			ObjectProvider<ClientRegistrationRepository> clientRegistrations,
			AuthenticationSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
		http
				.cors(cors -> { })
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/api/v1/system/**").permitAll()
						.requestMatchers("/api/v1/auth/session").permitAll()
						.anyRequest().authenticated());

		if (clientRegistrations.getIfAvailable() != null) {
			http.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler));
		}

		http.logout(logout -> logout
				.logoutUrl("/api/v1/auth/logout")
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID"));

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.web-origin:http://localhost:5173}") String webOrigin) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(webOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Content-Type", "X-Requested-With"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
