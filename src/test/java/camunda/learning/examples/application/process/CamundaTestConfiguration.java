package camunda.learning.examples.application.process;

import javax.sql.DataSource;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.extension.process_test_coverage.spring.SpringProcessWithCoverageEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Basic configuration for the JUnit-tests.
 * 
 * @author Ben Fuernrohr
 */
@Configuration
public class CamundaTestConfiguration {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public DataSource dataSource() {

        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:mem:camunda;DB_CLOSE_DELAY=-1;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {

        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration() {

        SpringProcessWithCoverageEngineConfiguration config = new SpringProcessWithCoverageEngineConfiguration();

        config.setExpressionManager(expressionManager());
        config.setTransactionManager(transactionManager());
        config.setDataSource(dataSource());
        config.setDatabaseSchemaUpdate("true");
        config.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        config.setJobExecutorActivate(false);

        config.init();
        return config;
    }

    @Bean
    ExpressionManager expressionManager() {
        return new SpringExpressionManager(applicationContext, null);
    }

    @Bean
    public ProcessEngineFactoryBean processEngine() {

        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }
}