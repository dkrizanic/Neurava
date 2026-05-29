package com.notebook.api.memory.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

@Component
class DeterministicEmbeddingGenerator implements EmbeddingGenerator {

	private static final int DIMENSIONS = 16;

	@Override
	public EmbeddingVector embed(String text) {
		byte[] digest = sha256(text == null ? "" : text);
		double[] values = new double[DIMENSIONS];
		for (int index = 0; index < DIMENSIONS; index++) {
			values[index] = ((digest[index] & 0xff) / 127.5d) - 1.0d;
		}
		return new EmbeddingVector(values);
	}

	private static byte[] sha256(String text) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}
