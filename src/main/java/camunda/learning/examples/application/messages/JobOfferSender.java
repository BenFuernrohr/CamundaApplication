package camunda.learning.examples.application.messages;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Message implementation for sending an applicant a job offer.
 * 
 * @author Ben Fuernrohr
 */
@Component
public class JobOfferSender implements JavaDelegate {

    @Autowired
    public RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution execution) {

        System.out.println(
            "Dear Applicant,\nwe are happy to inform you, that we would like to offer you the position you applied for.\nWe are looking forward to working with you!\nSincerely,\nCompany");
        runtimeService.createMessageCorrelation("InterviewInvitation");
    }
}
