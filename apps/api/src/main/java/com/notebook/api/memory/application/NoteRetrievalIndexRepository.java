package com.notebook.api.memory.application;

public interface NoteRetrievalIndexRepository {

	void upsert(NoteRetrievalIndexRecord record);
}
