package ru.elshin.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {
    @Bean
    public WorkflowClient workflowClient() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget("temporal:7233")
                .build();
        return WorkflowClient.newInstance(
                WorkflowServiceStubs.newInstance(options),
                WorkflowClientOptions.newBuilder().setNamespace("default").build()
        );
    }
}
