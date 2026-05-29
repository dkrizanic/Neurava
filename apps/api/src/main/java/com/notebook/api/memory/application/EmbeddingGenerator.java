package com.notebook.api.memory.application;

public interface EmbeddingGenerator {

	EmbeddingVector embed(String text);
}
