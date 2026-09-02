package ru.elshin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "appointment.notifications.queue";
    public static final String EXCHANGE_NAME = "appointment.exchange";

    // Routing keys для отправки точечных событий
    public static final String ROUTING_KEY_CREATED = "appointment.created";
    public static final String ROUTING_KEY_STATUS_CHANGED = "appointment.status.changed";

    // Новые константы для Saga (проверка пользователя)
    public static final String USER_VERIFICATION_EXCHANGE = "user-verification-exchange";
    public static final String USER_VERIFICATION_QUEUE = "user.verification.reply.queue";
    public static final String VERIFY_USER_ROUTING_KEY = "verify.user";

    // Новые константы для Saga (отправка команд в notification-service)
    public static final String NOTIFICATION_EXCHANGE = "notification-exchange";
    public static final String SEND_NOTIFICATION_ROUTING_KEY = "send.notification";

    // Новый бин обменника для уведомлений
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true); // durable = true (очередь выдержит перезапуск брокера)
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        // "appointment.#" перехватывает все routing keys: appointment.created, appointment.status.changed, etc.
        return BindingBuilder.bind(queue).to(exchange).with("appointment.#");
    }

    // БИНЫ ДЛЯ SAGA
    @Bean
    public TopicExchange userVerificationExchange() {
        return new TopicExchange(USER_VERIFICATION_EXCHANGE);
    }

    @Bean
    public Queue userVerificationReplyQueue() {
        return new Queue(USER_VERIFICATION_QUEUE, true);
    }

    @Bean
    public Binding userVerificationBinding(Queue userVerificationReplyQueue, TopicExchange userVerificationExchange) {
        return BindingBuilder.bind(userVerificationReplyQueue).to(userVerificationExchange).with("user.verified.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
