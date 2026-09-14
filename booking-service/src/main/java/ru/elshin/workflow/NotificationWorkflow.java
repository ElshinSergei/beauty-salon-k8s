package ru.elshin.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface NotificationWorkflow {
    @WorkflowMethod
    void sendNotification(String message, String target);
}
