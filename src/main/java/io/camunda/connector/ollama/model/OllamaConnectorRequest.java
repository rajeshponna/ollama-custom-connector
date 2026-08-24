package io.camunda.connector.ollama.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.PropertyType;
import jakarta.validation.constraints.NotBlank;

public class OllamaConnectorRequest {

    @TemplateProperty(
            group = "connection",
            label = "Base URL",
            description = "Ollama API root, e.g. http://localhost:11434"
    )
    private String baseUrl = "http://localhost:11434";

    @NotBlank
    @TemplateProperty(
            group = "connection",
            label = "Model",
            description = "Model tag as shown by 'ollama list', e.g. mistral:latest"
    )
    private String model;

    @TemplateProperty(
            group = "prompt",
            label = "System prompt",
            type = PropertyType.Text,
            description = "Instructions/role for the model. Optional.",
            optional = true
    )
    private String systemPrompt;

    @NotBlank
    @TemplateProperty(
            group = "prompt",
            label = "User prompt",
            type = PropertyType.Text,
            description = "The task/question for the model. Supports FEEL expressions."
    )
    private String userPrompt;

    @TemplateProperty(
            group = "prompt",
            label = "Temperature",
            description = "0.0 - 1.0. Lower is more deterministic."
    )
    private double temperature = 0.2;

    @TemplateProperty(
            group = "output",
            label = "Response format",
            description = "text = plain text, json = parsed into 'decision' map"
    )
    private String responseFormat = "text";

    @TemplateProperty(
            group = "output",
            label = "Timeout (seconds)",
            description = "HTTP timeout for Ollama response"
    )
    private int timeoutSeconds = 60;

    // Getters & Setters
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}