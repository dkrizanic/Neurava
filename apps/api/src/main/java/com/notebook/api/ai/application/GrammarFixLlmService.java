package com.notebook.api.ai.application;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GrammarFixLlmService {

	private static final String SYSTEM_PROMPT = """
			You are an expert copy editor.
			Task: fix only grammar, spelling, capitalization, punctuation, and spacing.
			Do not change meaning, tone, structure, or formatting intent.
			Do not add explanations.
			Return only the corrected text.
			""";

	private static final String PRETTIFY_SYSTEM_PROMPT = """
			You are an expert writing assistant for professional notes.
			Rewrite messy draft content into a polished, concise, clear note.
			Keep the original meaning and key facts.
			Return strict JSON only with this schema:
			{"title":"...","body":"...","tags":"tag1,tag2","linkedResources":"url1\\nurl2"}
			Rules:
			- title: 4-80 chars, specific and readable
			- body: structured paragraphs, polished wording, no markdown headings
			- tags: up to 4 comma-separated lowercase tags, empty string if none
			- linkedResources: newline-separated URLs found in input, empty string if none
			""";

	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String model;

	public GrammarFixLlmService(ChatModel chatModel,
			@Value("${spring.ai.openai.base-url:https://openrouter.ai/api}") String baseUrl,
			@Value("${spring.ai.openai.chat.options.model:openai/gpt-4o-mini}") String model) {
		this.chatClient = ChatClient.builder(chatModel).build();
		this.objectMapper = new ObjectMapper();
		this.baseUrl = baseUrl;
		this.model = model;
	}

	public String fix(String body) {
		String prompt = "Correct this text and return only the corrected text:\n\n" + body;
		String corrected = completionWithFallback(SYSTEM_PROMPT, prompt);

		if (corrected == null || corrected.isBlank()) {
			throw new IllegalStateException("LLM grammar fix returned empty content.");
		}

		return corrected.strip();
	}

	public NoteChangePreview prettifyDraft(String text) {
		String prompt = "Polish this draft into a professional note:\n\n" + text;
		String response = completionWithFallback(PRETTIFY_SYSTEM_PROMPT, prompt);
		String json = extractJsonObject(response);
		try {
			JsonNode root = this.objectMapper.readTree(json);
			String title = root.path("title").asText("").trim();
			String body = root.path("body").asText("").trim();
			String tags = root.path("tags").asText("").trim().toLowerCase(Locale.ROOT);
			String linkedResources = root.path("linkedResources").asText("").trim();

			if (title.isBlank()) {
				title = titleFromText(text);
			}
			if (body.isBlank()) {
				body = text.strip();
			}

			return new NoteChangePreview(title, body, tags, linkedResources);
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to parse prettified note response.", ex);
		}
	}

	private String completionWithFallback(String systemPrompt, String prompt) {
		try {
			String content = this.chatClient.prompt()
					.system(systemPrompt)
					.user(prompt)
					.call()
					.content();
			if (content != null && !content.isBlank()) {
				return content;
			}
		} catch (RuntimeException ignored) {
			// Continue to direct API call fallback.
		}

		return directCompletion(systemPrompt, prompt);
	}

	private String directCompletion(String systemPrompt, String prompt) {
		String apiKey = resolveApiKey();
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("LLM API key is not configured.");
		}

		String endpoint = completionsEndpoint(this.baseUrl);
		String payload;
		try {
			payload = this.objectMapper.writeValueAsString(new ChatRequest(this.model,
					new Message[] {
							new Message("system", systemPrompt),
							new Message("user", prompt),
					}));
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to prepare LLM request payload.", ex);
		}

		HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(payload))
				.build();

		HttpResponse<String> response;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new IllegalStateException("LLM request failed.", ex);
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("LLM request failed with status " + response.statusCode() + ".");
		}

		try {
			JsonNode root = this.objectMapper.readTree(response.body());
			return root.path("choices").path(0).path("message").path("content").asText("");
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to parse LLM response.", ex);
		}
	}

	private static String completionsEndpoint(String baseUrl) {
		String normalized = baseUrl == null ? "" : baseUrl.strip();
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.endsWith("/v1")) {
			return normalized + "/chat/completions";
		}
		return normalized + "/v1/chat/completions";
	}

	private static String resolveApiKey() {
		String fromEnv = System.getenv("OPENAI_API_KEY");
		if (fromEnv != null && !fromEnv.isBlank()) {
			return fromEnv;
		}
		fromEnv = System.getenv("OPENROUTER_API_KEY");
		if (fromEnv != null && !fromEnv.isBlank()) {
			return fromEnv;
		}

		for (Path candidate : dotenvCandidates()) {
			String value = readKeyFromDotEnv(candidate, "OPENAI_API_KEY");
			if (value != null && !value.isBlank()) {
				return value;
			}
			value = readKeyFromDotEnv(candidate, "OPENROUTER_API_KEY");
			if (value != null && !value.isBlank()) {
				return value;
			}
		}

		return "";
	}

	private static String extractJsonObject(String content) {
		if (content == null) {
			return "{}";
		}
		String trimmed = content.strip();
		if (trimmed.startsWith("```")) {
			int firstNewline = trimmed.indexOf('\n');
			int lastFence = trimmed.lastIndexOf("```");
			if (firstNewline >= 0 && lastFence > firstNewline) {
				trimmed = trimmed.substring(firstNewline + 1, lastFence).strip();
			}
		}
		int firstBrace = trimmed.indexOf('{');
		int lastBrace = trimmed.lastIndexOf('}');
		if (firstBrace >= 0 && lastBrace > firstBrace) {
			return trimmed.substring(firstBrace, lastBrace + 1);
		}
		return trimmed;
	}

	private static String titleFromText(String text) {
		String normalized = text == null ? "" : text.strip().replaceAll("\\s+", " ");
		if (normalized.isBlank()) {
			return "Untitled note";
		}
		if (normalized.length() <= 80) {
			return normalized;
		}
		return normalized.substring(0, 77).stripTrailing() + "...";
	}

	private static List<Path> dotenvCandidates() {
		return List.of(
				Path.of(".env"),
				Path.of("..", ".env"),
				Path.of("..", "..", ".env"),
				Path.of("apps", "api", ".env"),
				Path.of("apps", "..", ".env"));
	}

	private static String readKeyFromDotEnv(Path file, String key) {
		if (!Files.exists(file)) {
			return "";
		}
		try {
			for (String rawLine : Files.readAllLines(file, StandardCharsets.UTF_8)) {
				String line = rawLine.strip();
				if (line.isBlank() || line.startsWith("#") || !line.startsWith(key + "=")) {
					continue;
				}
				String value = line.substring((key + "=").length()).trim();
				if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
					value = value.substring(1, value.length() - 1);
				}
				return value;
			}
		} catch (IOException ignored) {
			return "";
		}
		return "";
	}

	private record Message(String role, String content) {
	}

	private record ChatRequest(String model, Message[] messages) {
	}
}