package com.notebook.api.auth.infrastructure.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.CurrentSession;
import com.notebook.api.auth.application.CurrentSessionService;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/auth")
class AuthSessionController {

	private final CurrentSessionService sessions;

	AuthSessionController(CurrentSessionService sessions) {
		this.sessions = sessions;
	}

	@GetMapping("/session")
	CurrentSession currentSession(Authentication authentication) {
		return this.sessions.currentSession(authentication);
	}
}
