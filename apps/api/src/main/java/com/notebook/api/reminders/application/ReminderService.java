package com.notebook.api.reminders.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.notebook.api.integrations.application.IntegrationService;
import com.notebook.api.reminders.domain.Reminder;
import com.notebook.api.reminders.infrastructure.persistence.ReminderRepository;

@Service
public class ReminderService {

	private final ReminderRepository reminders;
	private final IntegrationService integrations;

	public ReminderService(ReminderRepository reminders, IntegrationService integrations) {
		this.reminders = reminders;
		this.integrations = integrations;
	}

	@Transactional(readOnly = true)
	public List<ReminderSummary> list(UUID workspaceContextId, boolean includeCompleted) {
		return this.reminders.findByWorkspaceContextIdOrderByDueAtAsc(workspaceContextId).stream()
				.filter(reminder -> includeCompleted || reminder.getCompletedAt() == null)
				.map(ReminderSummary::from)
				.toList();
	}

	@Transactional
	public ReminderSummary create(UUID ownerAccountId, UUID workspaceContextId, String title, String details,
			Instant dueAt, String relatedContext, boolean calendarSyncEnabled) {
		boolean calendarEnabled = this.integrations.isCalendarEnabled(workspaceContextId);
		Reminder saved = this.reminders.save(Reminder.create(ownerAccountId, workspaceContextId, title, details, dueAt,
				relatedContext, calendarSyncEnabled, calendarEnabled, Instant.now()));
		return ReminderSummary.from(saved);
	}

	@Transactional
	public ReminderSummary update(UUID reminderId, UUID workspaceContextId, String title, String details, Instant dueAt,
			String relatedContext, boolean calendarSyncEnabled) {
		Reminder reminder = findWorkspaceReminder(reminderId, workspaceContextId);
		boolean calendarEnabled = this.integrations.isCalendarEnabled(workspaceContextId);
		reminder.update(title, details, dueAt, relatedContext, calendarSyncEnabled, calendarEnabled, Instant.now());
		return ReminderSummary.from(reminder);
	}

	@Transactional
	public ReminderSummary complete(UUID reminderId, UUID workspaceContextId) {
		Reminder reminder = findWorkspaceReminder(reminderId, workspaceContextId);
		reminder.complete(Instant.now());
		return ReminderSummary.from(reminder);
	}

	@Transactional
	public ReminderSummary reopen(UUID reminderId, UUID workspaceContextId) {
		Reminder reminder = findWorkspaceReminder(reminderId, workspaceContextId);
		reminder.reopen(Instant.now());
		return ReminderSummary.from(reminder);
	}

	@Transactional
	public void delete(UUID reminderId, UUID workspaceContextId) {
		this.reminders.delete(findWorkspaceReminder(reminderId, workspaceContextId));
	}

	private Reminder findWorkspaceReminder(UUID reminderId, UUID workspaceContextId) {
		return this.reminders.findByIdAndWorkspaceContextId(reminderId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found."));
	}
}
