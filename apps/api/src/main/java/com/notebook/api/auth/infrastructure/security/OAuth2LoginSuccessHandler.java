package com.notebook.api.auth.infrastructure.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.notebook.api.auth.application.AuthAccountService;

@Component
class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthAccountService accounts;
	private final String webOrigin;

	OAuth2LoginSuccessHandler(AuthAccountService accounts, @Value("${app.web-origin:http://localhost:5173}") String webOrigin) {
		this.accounts = accounts;
		this.webOrigin = webOrigin.split(",")[0].trim();
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		this.accounts.createOrUpdateFrom(authentication);
		response.sendRedirect(this.webOrigin);
	}
}
