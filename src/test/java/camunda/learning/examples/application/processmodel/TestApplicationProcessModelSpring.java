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

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mockito.DelegateExpressions;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemProcessEngineConfiguration.class})
public class TestApplicationProcessModelSpring {

    @Autowired
    ProcessEngine processEngine;

    @Rule
    @ClassRule
    public static ProcessEngineRule rule;

    @PostConstruct
    void initRule() {
        rule = TestCoverageProcessEngineRuleBuilder.create(processEngine).build();
    }

    @Before
    public void setUp() {
        // mock user tasks and service tasks
        autoMock("application.bpmn");

        // register mocks for message send event delegates, as those are not handled by autoMock
        DelegateExpressions.registerJavaDelegateMock("interviewInvitationSender");
        DelegateExpressions.registerJavaDelegateMock("jobOfferSender");
        DelegateExpressions.registerJavaDelegateMock("applicationDenialSender");
    }

    @Test
    @Ignore
    @Deployment(resources = "application.bpmn")
    public void start_and_finish_process() {

        // specify behaviour of applicationTester
        DelegateExpressions.getJavaDelegateMock("applicationTester")
            .onExecutionSetVariables(Variables.createVariables().putValue("application_complete", true));

        // start process instance by message
        final ProcessInstance processInstance = this.processEngine.getRuntimeService()
            .startProcessInstanceByMessage("newApplication", "businessKey");
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

        complete(secondOpinionTask, withVariables("Positive_Impression", false));
        assertThat(processInstance).isNotWaitingAt("interview");
        complete(firstOpinionTask, withVariables("Positive_Impression", true));

        // have successful interview
        assertThat(processInstance).isWaitingAtExactly("interview");
        complete(task(), withVariables("succesful", true));

        // verify mocks executed
        verifyJavaDelegateMock("applicationTester").executed();
        verifyJavaDelegateMock("interviewInvitationSender").executed();
        verifyJavaDelegateMock("jobOfferSender").executed();
    }
}
