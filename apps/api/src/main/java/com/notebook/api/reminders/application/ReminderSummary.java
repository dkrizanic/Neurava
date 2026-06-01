package com.notebook.api.reminders.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.reminders.domain.CalendarSyncState;
import com.notebook.api.reminders.domain.Reminder;

public record ReminderSummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String title,
		String details,
		Instant dueAt,
		String relatedContext,
		boolean calendarSyncEnabled,
		CalendarSyncState calendarSyncState,
		String calendarEventId,
		Instant createdAt,
		Instant updatedAt,
		Instant completedAt
) {

	public static ReminderSummary from(Reminder reminder) {
		return new ReminderSummary(
				reminder.getId(),
				reminder.getOwnerAccountId(),
				reminder.getWorkspaceContextId(),
				reminder.getTitle(),
				reminder.getDetails(),
				reminder.getDueAt(),
				reminder.getRelatedContext(),
				reminder.isCalendarSyncEnabled(),
				reminder.getCalendarSyncState(),
				reminder.getCalendarEventId(),
				reminder.getCreatedAt(),
				reminder.getUpdatedAt(),
				reminder.getCompletedAt());
	}
}

