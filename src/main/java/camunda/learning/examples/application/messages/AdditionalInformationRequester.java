package camunda.learning.examples.application.messages;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Message implementation for requesting additional information.
 * 
 * @author Ben Fuernrohr
 */
@Component
public class AdditionalInformationRequester implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println(
            "Dear applicant,\nadditional informations are required to further your application.\nSincerely,\nCompany");
    }

}
