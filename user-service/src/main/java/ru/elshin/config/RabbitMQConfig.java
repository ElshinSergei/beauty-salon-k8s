package ru.elshin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Та же константа, что и в booking-service
    public static final String VERIFY_USER_QUEUE = "user.verification.command.queue";
    public static final String USER_VERIFICATION_EXCHANGE = "user-verification-exchange";

    @Bean
    public TopicExchange userVerificationExchange() {
        return new TopicExchange(USER_VERIFICATION_EXCHANGE);
    }

    @Bean
    public Queue verifyUserCommandQueue() {
        return new Queue(VERIFY_USER_QUEUE, true);
    }

    // Нужно забиндить эту очередь к exchange
    @Bean
    public Binding binding(Queue verifyUserQueue, TopicExchange userVerificationExchange) {
        return BindingBuilder.bind(verifyUserQueue).to(userVerificationExchange).with("verify.user");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
