package camunda.learning.examples.application;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main class running the application with camunda.
 * 
 * @author Ben Fuernrohr
 */
@EnableAutoConfiguration
@SpringBootApplication
@EnableProcessApplication("ApplicationApp")
@ComponentScan(basePackages = "camunda.learning.examples.application")
public class ApplicationApp {

    public static void main(String... args) {
        SpringApplication.run(ApplicationApp.class, args);
    }

}