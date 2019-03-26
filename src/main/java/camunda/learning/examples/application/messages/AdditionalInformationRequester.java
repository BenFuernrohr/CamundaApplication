package camunda.learning.examples.application.messages;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class AdditionalInformationRequester implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println(
            "Dear applicant,\nadditional informations are required to further your application.\nSincerely,\nCompany");
    }

}
