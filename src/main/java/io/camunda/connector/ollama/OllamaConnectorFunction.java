package io.camunda.example;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.example.model.OllamaConnectorRequest;
import io.camunda.example.model.OllamaConnectorResult;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

@OutboundConnector(
        name = "OLLAMA_CHAT",
        inputVariables = {
                "baseUrl", "model", "systemPrompt",
                "userPrompt", "temperature",
                "responseFormat", "timeoutSeconds"
        },
        type = "io.camunda:ollama-chat:1"
)
@ElementTemplate(
        id = "io.camunda.connectors.OllamaChat.v1",
        name = "Ollama Chat",
        version = 1,
        description = "Call a local or remote Ollama model and use its response to drive process decisions.",
        icon = "icon.svg",
        documentationRef = "https://docs.ollama.com/api",
        inputDataClass = OllamaConnectorRequest.class
)
public class OllamaConnectorFunction implements OutboundConnectorFunction {

  private static final Logger log = LoggerFactory.getLogger(OllamaConnectorFunction.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public OllamaConnectorResult execute(OutboundConnectorContext context) throws Exception {

    OllamaConnectorRequest request = context.bindVariables(OllamaConnectorRequest.class);

    log.info("Ollama connector | model={} | format={}", request.getModel(), request.getResponseFormat());

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    List<Map<String, String>> messages = new ArrayList<>();
    if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
      messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
    }
    messages.add(Map.of("role", "user", "content", request.getUserPrompt()));

    Map<String, Object> bodyMap = new LinkedHashMap<>();
    bodyMap.put("model", request.getModel());
    bodyMap.put("messages", messages);
    bodyMap.put("stream", false);
    bodyMap.put("options", Map.of("temperature", request.getTemperature()));

    if ("json".equals(request.getResponseFormat())) {
      bodyMap.put("format", "json");
    }

    String bodyJson = MAPPER.writeValueAsString(bodyMap);
    String url = request.getBaseUrl().stripTrailing() + "/api/chat";

    Request httpRequest = new Request.Builder()
            .url(url)
            .post(RequestBody.create(bodyJson, MediaType.parse("application/json")))
            .build();

    OllamaConnectorResult result = new OllamaConnectorResult();

    try (Response response = client.newCall(httpRequest).execute()) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Ollama API error: HTTP " + response.code()
                + " — " + response.body().string());
      }

      String responseBody = response.body().string();
      JsonNode root = MAPPER.readTree(responseBody);
      String content = root.path("message").path("content").asText("");
      String modelUsed = root.path("model").asText(request.getModel());
      boolean done = root.path("done").asBoolean(true);

      result.setResponseText(content);
      result.setModel(modelUsed);
      result.setDone(done);

      if ("json".equals(request.getResponseFormat())) {
        try {
          Map<String, Object> parsed = MAPPER.readValue(
                  content,
                  MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
          );
          result.setDecision(parsed);
          log.info("Ollama JSON decision: {}", parsed);
        } catch (Exception e) {
          log.warn("Could not parse JSON from Ollama: {}", content);
          result.setDecision(Map.of("raw", content));
        }
      }

      log.info("✅ Ollama done | model={}", modelUsed);
    }

    return result;
  }
}