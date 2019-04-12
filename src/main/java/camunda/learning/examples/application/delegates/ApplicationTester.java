package camunda.learning.examples.application.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import camunda.learning.examples.application.variables.ApplicationProcessVariables;

/**
 * Service Task implementation for validating a new application.
 * 
 * @author Ben Fuernrohr
 */
@Component
public class ApplicationTester implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        ApplicationProcessVariables variables = new ApplicationProcessVariables(execution);

        String message = variables.getApplicationContent();
        if (message.length() < 10) {
            System.out.println("Application invalid!");
            execution.setVariable("application_complete", false);
        } else {
            System.out.println("Application valid! Congratulations!");
            execution.setVariable("application_complete", true);
        }
    }
}
