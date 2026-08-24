package io.camunda.connector.ollama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is a minimal Spring Boot application to run the connector locally for testing purposes.
 */
@SpringBootApplication
public class LocalConnectorRuntime {

  public static void main(String[] args) {
    SpringApplication.run(LocalConnectorRuntime.class, args);
  }
}
