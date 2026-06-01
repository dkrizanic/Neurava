package com.notebook.api.integrations.application;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.integrations.domain.IntegrationConnection;
import com.notebook.api.integrations.domain.IntegrationProvider;
import com.notebook.api.integrations.infrastructure.persistence.IntegrationConnectionRepository;

@Service
public class IntegrationService {

	private final IntegrationConnectionRepository connections;

	public IntegrationService(IntegrationConnectionRepository connections) {
		this.connections = connections;
	}

	@Transactional(readOnly = true)
	public List<IntegrationSummary> list(UUID workspaceContextId) {
		return this.connections.findByWorkspaceContextIdOrderByProviderAsc(workspaceContextId).stream()
				.map(IntegrationSummary::from)
				.toList();
	}

	@Transactional
	public IntegrationSummary connect(UUID ownerAccountId, UUID workspaceContextId, IntegrationProvider provider) {
		Instant now = Instant.now();
		IntegrationConnection connection = this.connections
				.findByWorkspaceContextIdAndProvider(workspaceContextId, provider)
				.orElseGet(() -> IntegrationConnection.connect(ownerAccountId, workspaceContextId, provider,
						permissionSummary(provider), now));
		connection.connect(permissionSummary(provider), now);
		return IntegrationSummary.from(this.connections.save(connection));
	}

	@Transactional
	public IntegrationSummary disconnect(UUID workspaceContextId, IntegrationProvider provider) {
		IntegrationConnection connection = this.connections.findByWorkspaceContextIdAndProvider(workspaceContextId,
				provider).orElseGet(() -> IntegrationConnection.connect(UUID.randomUUID(), workspaceContextId, provider,
						permissionSummary(provider), Instant.now()));
		connection.disconnect(Instant.now());
		return IntegrationSummary.from(this.connections.save(connection));
	}

	@Transactional(readOnly = true)
	public boolean isEnabled(UUID workspaceContextId, IntegrationProvider provider) {
		return this.connections.findByWorkspaceContextIdAndProvider(workspaceContextId, provider)
				.map(IntegrationConnection::isEnabled)
				.orElse(false);
	}

	@Transactional(readOnly = true)
	public boolean isCalendarEnabled(UUID workspaceContextId) {
		return isEnabled(workspaceContextId, IntegrationProvider.CALENDAR);
	}

	@Transactional(readOnly = true)
	public List<CalendarEventSummary> upcomingCalendarEvents(UUID workspaceContextId) {
		if (!isEnabled(workspaceContextId, IntegrationProvider.CALENDAR)) {
			return List.of();
		}
		Instant now = Instant.now();
		return List.of(
				new CalendarEventSummary("local-calendar-context", "Planning focus block", now.plus(Duration.ofDays(1)),
						now.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)), "Notebook Calendar"),
				new CalendarEventSummary("local-calendar-review", "Weekly review", now.plus(Duration.ofDays(3)),
						now.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(45)), "Notebook Calendar"));
	}

	private static String permissionSummary(IntegrationProvider provider) {
		return switch (provider) {
			case CALENDAR -> "Read upcoming calendar context and create linked reminder events when sync is enabled.";
			case GMAIL -> "Search Gmail only after opt-in and show Gmail source references in AI answers.";
		};
	}
}
