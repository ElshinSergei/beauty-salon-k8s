package ru.elshin.worker;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.elshin.activity.NotificationActivityImpl;
import ru.elshin.workflow.NotificationWorkflowImpl;

@Component
public class TemporalWorker {

    private final WorkflowClient workflowClient;
    private WorkerFactory factory;

    @Autowired
    public TemporalWorker(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostConstruct // Использовать аннотацию, чтобы запустить при старте
    public void startWorker(NotificationActivityImpl activity) {

        this.factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker("NOTIFICATION_TASK_QUEUE");

        // Регистрируем вашу активность
        worker.registerActivitiesImplementations(activity);
        // РЕГИСТРАЦИЯ WORKFLOW
        worker.registerWorkflowImplementationTypes(NotificationWorkflowImpl.class);

        factory.start();
    }

    @PreDestroy
    public void stopWorker() {
        if (factory != null) {
            // Останавливаем фабрику при выключении приложения
            factory.shutdown();
        }
    }
}
