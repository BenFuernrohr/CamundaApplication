package camunda.learning.examples.application.messages;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ApplicationDenialSender implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println(
            "Dear Applicant,\nwe regret to inform you that your application has been declined.\nWe wish you best of luck with your future endeavors!\nSincerely,\nCompany");

    }

}
