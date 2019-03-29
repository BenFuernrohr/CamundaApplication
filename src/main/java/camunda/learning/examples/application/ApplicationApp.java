package camunda.learning.examples.application;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
@ProcessApplication
public class ApplicationApp {

    @Autowired
    RuntimeService runtimeService;

    public static void main(String... args) {
        SpringApplication.run(ApplicationApp.class, args);
    }
}