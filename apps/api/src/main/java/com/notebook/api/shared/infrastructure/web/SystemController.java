package com.notebook.api.shared.infrastructure.web;

import java.time.Instant;
import java.util.List;

import com.notebook.api.shared.infrastructure.config.AppInfoProperties;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/system")
public class SystemController {

	private final AppInfoProperties appInfo;
	private final Environment environment;

	public SystemController(AppInfoProperties appInfo, Environment environment) {
		this.appInfo = appInfo;
		this.environment = environment;
	}

	@GetMapping("/version")
	public VersionResponse version() {
		return new VersionResponse(
				appInfo.name(),
				appInfo.version(),
				profiles(),
				Instant.now());
	}

	private List<String> profiles() {
		String[] activeProfiles = environment.getActiveProfiles();
		return activeProfiles.length == 0 ? List.of(environment.getDefaultProfiles()) : List.of(activeProfiles);
	}

	public record VersionResponse(String name, String version, List<String> profiles, Instant serverTime) {
	}
}
