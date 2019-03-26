package camunda.learning.examples.application.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ApplicationTester implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String message = (String) execution.getVariable("applicationContent");
        if (message.length() < 10) {
            System.out.println("Application invalid!");
            execution.setVariable("application_complete", false);
        } else {
            System.out.println("Application valid! Congratulations!");
            execution.setVariable("application_complete", true);
        }
    }
}
