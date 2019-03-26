package camunda.learning.examples.application.messages;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class InterviewInvitationSender implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println(
            "Dear Applicant,\nwe would like to invite you to a job interview taking place at our company building next monday at 10 am.\nSincerely,\nCompany");

    }

}
