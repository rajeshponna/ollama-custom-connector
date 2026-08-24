package io.camunda.connector.ollama.model;



import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaConnectorResult {

    private String responseText;
    private Map<String, Object> decision;
    private String model;
    private boolean done;

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public Map<String, Object> getDecision() { return decision; }
    public void setDecision(Map<String, Object> decision) { this.decision = decision; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}