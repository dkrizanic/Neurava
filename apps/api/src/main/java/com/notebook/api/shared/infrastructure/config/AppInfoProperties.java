package com.notebook.api.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.info")
public record AppInfoProperties(String name, String version) {

	public AppInfoProperties {
		if (name == null || name.isBlank()) {
			name = "Neurava API";
		}
		if (version == null || version.isBlank()) {
			version = "0.0.1-SNAPSHOT";
		}
	}
}
