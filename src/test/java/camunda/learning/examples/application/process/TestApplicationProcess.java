package camunda.learning.examples.application.process;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.taskQuery;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Testclass for the application process model.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CamundaTestConfiguration.class})
public class TestApplicationProcess {

    @Autowired
    ProcessEngine processEngine;

    @Rule
    @ClassRule
    public static ProcessEngineRule processEngineRule;

    @PostConstruct
    void initRule() {
        // this approach is required for the graphical coverage-output
        processEngineRule = TestCoverageProcessEngineRuleBuilder.create(processEngine).build();
    }

    @Test
    @Deployment(resources = "application.bpmn")
    public void process_instance_should_start_upon_message() {

        ProcessInstance processInstance = TestApplicationProcess.processEngineRule.getRuntimeService()
            .startProcessInstanceByMessage("newApplication", createVariablesForValidApplication());
        assertThat(processInstance).isStarted();
    }

    @Test
    @Deployment(resources = "application.bpmn")
    public void process_instance_with_invalid_message_content_should_wait_for_additional_info() {

        ProcessInstance processInstance = TestApplicationProcess.processEngineRule.getRuntimeService()
            .startProcessInstanceByMessage("newApplication", createVariablesForInvalidApplication());
        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAtExactly("waitingForAdditionalInformations");
        // assertThat(processInstance).isWaitingFor("additionalInfoMessage");
    }

    @Test
    @Deployment(resources = "application.bpmn")
    public void process_instance_with_valid_message_content_should_wait_at_user_task_validation() {

        ProcessInstance processInstance = TestApplicationProcess.processEngineRule.getRuntimeService()
            .startProcessInstanceByMessage("newApplication", createVariablesForValidApplication());
        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAtExactly("qualifyApplicant");
    }

    @Test
    @Deployment(resources = "application.bpmn")
    public void test_happy_path_of_process() {
        ProcessInstance processInstance = TestApplicationProcess.processEngineRule.getRuntimeService()
            .startProcessInstanceByMessage("newApplication", createVariablesForValidApplication());

        // qualify applicant
        assertThat(processInstance).isWaitingAtExactly("qualifyApplicant");
        complete(task(), withVariables("approved", true));

        // get one positive opinion
        assertEquals(taskQuery().list().size(), 2);
        Task firstOpinionTask = taskQuery().list().get(0);
        Task secondOpinionTask = taskQuery().list().get(1);
        assertEquals("getOpinion", firstOpinionTask.getTaskDefinitionKey());
        assertEquals("getOpinion", secondOpinionTask.getTaskDefinitionKey());
        assertNotEquals(firstOpinionTask.getId(), secondOpinionTask.getId());
        assertThat(processInstance).isWaitingAt("getOpinion");

        complete(secondOpinionTask, withVariables("Positive_Impression", false));
        assertThat(processInstance).isNotWaitingFor("interview");
        complete(firstOpinionTask, withVariables("Positive_Impression", true));

        // have successful interview
        assertThat(processInstance).isWaitingAtExactly("interview");
        complete(task(), withVariables("succesful", true));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasNotPassed("requestAdditionalInformations");
        assertThat(processInstance).hasNotPassed("declineApplicant");
        assertThat(processInstance).hasPassed("approveApplicant");
    }

    private static Map<String, Object> createVariablesForValidApplication() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applicationContent", "Hi, my name is Alan Harper and i´d like to apply, please!");
        return variables;
    }

    private static Map<String, Object> createVariablesForInvalidApplication() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applicationContent", "Hi!!");
        return variables;
    }
}