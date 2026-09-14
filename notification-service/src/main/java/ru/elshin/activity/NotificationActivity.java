package ru.elshin.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface NotificationActivity {

    @ActivityMethod
    void sendNotification(String message, String target);

}
