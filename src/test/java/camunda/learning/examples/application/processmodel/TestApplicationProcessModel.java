package camunda.learning.examples.application.processmodel;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.autoMock;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.verifyJavaDelegateMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mockito.DelegateExpressions;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Testclass for the application process model, solely as a unit test without spring boot. Generates an HTML-File
 * showing the coverage of the tests.
 */
@Deployment(resources = "application.bpmn")
public class TestApplicationProcessModel {

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    private static RuntimeService runtimeService;

    @Before
    public void setUp() {
        runtimeService = TestApplicationProcessModel.rule.getRuntimeService();

        // mock service tasks with delegate expressions
        autoMock("application.bpmn");

        // register mocks for message send event delegates
        DelegateExpressions.registerJavaDelegateMock("interviewInvitationSender");
        DelegateExpressions.registerJavaDelegateMock("jobOfferSender");
        DelegateExpressions.registerJavaDelegateMock("applicationDenialSender");
        DelegateExpressions.registerJavaDelegateMock("additionalInformationRequester");
    }

    @Test
    public void test_happy_path_succesful_application() {

        // specify behaviour of applicationTester to positive output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // do manual qualification
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

        complete(secondOpinionTask, withVariables("positive_Impression", false));
        assertThat(processInstance).isNotWaitingAt("interview");
        complete(firstOpinionTask, withVariables("positive_Impression", true));

        // have successful interview
        assertThat(processInstance).isWaitingAtExactly("interview");
        complete(task(), withVariables("succesful", true));

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executed();
        verifyJavaDelegateMock("jobOfferSender").executed();
        verifyJavaDelegateMock("applicationDenialSender").executedNever();
        verifyJavaDelegateMock("additionalInformationRequester").executedNever();
    }

    @Test
    public void test_fail_test_of_application() {
        // specify behaviour of applicationTester to negative output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", false));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // receive additional informations
        assertThat(processInstance).isWaitingAtExactly("waitingForAdditionalInfos");
        runtimeService.correlateMessage("additionalInformationMessage");

        assertThat(processInstance).isWaitingAtExactly("checkApplication");

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("additionalInformationRequester").executed();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("applicationDenialSender").executedNever();
        verifyJavaDelegateMock("interviewInvitationSender").executedNever();
    }

    @Test
    public void test_declinement_of_manual_qualification() {
        // specify behaviour of applicationTester to positive output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application successfully
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // fail manual qualification
        assertThat(processInstance).isWaitingAtExactly("qualifyApplicant");
        complete(task(), withVariables("approved", false));

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("applicationDenialSender").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executedNever();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("additionalInformationRequester").executedNever();
    }

    @Test
    public void test_declinement_of_positive_opinion() {
        // specify behaviour of applicationTester to positive output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application successfully
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // do manual qualification
        assertThat(processInstance).isWaitingAtExactly("qualifyApplicant");
        complete(task(), withVariables("approved", true));

        // get two negative opinions
        assertEquals(taskQuery().list().size(), 2);
        Task firstOpinionTask = taskQuery().list().get(0);
        Task secondOpinionTask = taskQuery().list().get(1);
        assertEquals("getOpinion", firstOpinionTask.getTaskDefinitionKey());
        assertEquals("getOpinion", secondOpinionTask.getTaskDefinitionKey());
        assertNotEquals(firstOpinionTask.getId(), secondOpinionTask.getId());
        assertThat(processInstance).isWaitingAt("getOpinion");

        complete(secondOpinionTask, withVariables("positive_Impression", false));
        assertThat(processInstance).isNotWaitingAt("interview");
        complete(firstOpinionTask, withVariables("positive_Impression", false));

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("applicationDenialSender").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executedNever();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("additionalInformationRequester").executedNever();
    }

    @Test
    public void test_unsuccesful_interview() {
        // specify behaviour of applicationTester to positive output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // do manual qualification
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

        complete(secondOpinionTask, withVariables("positive_Impression", false));
        assertThat(processInstance).isNotWaitingAt("interview");
        complete(firstOpinionTask, withVariables("positive_Impression", true));

        // fail interview
        assertThat(processInstance).isWaitingAtExactly("interview");
        complete(task(), withVariables("succesful", false));

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executed();
        verifyJavaDelegateMock("applicationDenialSender").executed();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("additionalInformationRequester").executedNever();
    }

    @Test
    public void test_timer_on_additional_informations() {
        // specify behaviour of applicationTester to negative output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", false));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // trigger intermediate timer event
        assertThat(processInstance).isWaitingAtExactly("waitingForAdditionalInfos");
        execute(job());

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("additionalInformationRequester").executed();
        verifyJavaDelegateMock("applicationDenialSender").executed();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("interviewInvitationSender").executedNever();
    }

    @Test
    public void test_interruption_via_cancellation() {
        // specify behaviour of applicationTester to positive output
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newApplication",
            "businessKey");
        assertThat(processInstance).isStarted();

        // check application
        assertThat(processInstance).isWaitingAtExactly("checkApplication");
        execute(job());

        // do manual qualification
        assertThat(processInstance).isWaitingAtExactly("qualifyApplicant");
        complete(task(), withVariables("approved", true));

        // get cancellation from applicant
        // runtimeService.correlateMessage("cancellationMessage");
        runtimeService.createMessageCorrelation("cancellationMessage").correlate();

        assertThat(processInstance).isEnded();

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executedNever();
        verifyJavaDelegateMock("applicationDenialSender").executedNever();
        verifyJavaDelegateMock("jobOfferSender").executedNever();
        verifyJavaDelegateMock("additionalInformationRequester").executedNever();
    }
}
