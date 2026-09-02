package ru.elshin.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "appointment.notifications.queue";

    // Новая очередь и обменник для Саги
    public static final String NOTIFICATION_COMMAND_QUEUE = "notification.command.queue";
    public static final String NOTIFICATION_EXCHANGE = "notification-exchange";

    // Теперь сервис сам создаст очередь в RabbitMQ при старте, если её там нет
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true); // durable = true
    }

    @Bean
    public Queue notificationSagaQueue() {
        return new Queue(NOTIFICATION_COMMAND_QUEUE, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding sagaBinding(Queue notificationSagaQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationSagaQueue).to(notificationExchange).with("send.notification");
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
