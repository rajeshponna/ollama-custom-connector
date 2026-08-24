package io.camunda.connector.ollama;

import io.camunda.connector.ollama.model.OllamaConnectorResult;
import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaConnectorTest {

  private MockWebServer mockServer;
  private OllamaConnectorFunction connector;

  @BeforeEach
  void setUp() throws Exception {
    mockServer = new MockWebServer();
    mockServer.start();
    connector = new OllamaConnectorFunction();
  }

  @AfterEach
  void tearDown() throws Exception {
    mockServer.shutdown();
  }

  @Test
  void shouldCallOllamaAndReturnText() throws Exception {
    // Mock Ollama response
    mockServer.enqueue(new MockResponse()
            .setBody("""
                    {
                      "model": "mistral",
                      "message": {"role": "assistant", "content": "Hello!"},
                      "done": true
                    }
                    """)
            .addHeader("Content-Type", "application/json"));

    String baseUrl = mockServer.url("").toString().replaceAll("/$", "");

    var context = OutboundConnectorContextBuilder.create()
            .variables(Map.of(
                    "baseUrl", baseUrl,
                    "model", "mistral",
                    "systemPrompt", "You are helpful.",
                    "userPrompt", "Say hello",
                    "temperature", 0.1,
                    "responseFormat", "text",
                    "timeoutSeconds", 30
            ))
            .build();

    OllamaConnectorResult result = connector.execute(context);

    assertThat(result).isNotNull();
    assertThat(result.getResponseText()).isEqualTo("Hello!");
    assertThat(result.isDone()).isTrue();
    assertThat(result.getModel()).isEqualTo("mistral");
    System.out.println("✅ Response: " + result.getResponseText());
  }

  @Test
  void shouldParseJsonResponse() throws Exception {
    mockServer.enqueue(new MockResponse()
            .setBody("""
                    {
                      "model": "mistral",
                      "message": {"role": "assistant", "content": "{\\"action\\": \\"approve\\", \\"confidence\\": 0.95}"},
                      "done": true
                    }
                    """)
            .addHeader("Content-Type", "application/json"));

    String baseUrl = mockServer.url("").toString().replaceAll("/$", "");

    var context = OutboundConnectorContextBuilder.create()
            .variables(Map.of(
                    "baseUrl", baseUrl,
                    "model", "mistral",
                    "userPrompt", "Approve or reject?",
                    "temperature", 0.0,
                    "responseFormat", "json",
                    "timeoutSeconds", 30
            ))
            .build();

    OllamaConnectorResult result = connector.execute(context);

    assertThat(result.getDecision()).isNotNull();
    assertThat(result.getDecision()).containsKey("action");
    System.out.println("✅ Decision: " + result.getDecision());
  }
}