package ru.elshin.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import ru.elshin.activity.NotificationActivity;

import java.time.Duration;

public class NotificationWorkflowImpl implements NotificationWorkflow {

    // Создаем "заглушку" для вызова Activity
    private final NotificationActivity activity = Workflow.newActivityStub(
            NotificationActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void sendNotification(String message, String target) {
        // Здесь мы оркестрируем: просто вызываем активность
        activity.sendNotification(message, target);
    }
}
