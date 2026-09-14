package ru.elshin.activity;

import org.springframework.stereotype.Service;

@Service
public class NotificationActivityImpl implements NotificationActivity {

    @Override
    public void sendNotification(String message, String target) {
        // Здесь ваша реальная логика (SMTP, SMS-шлюз и т.д.)
        System.out.println("Отправка уведомления на " + target + ": " + message);
    }
}
