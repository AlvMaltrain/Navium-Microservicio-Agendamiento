package com.navium.agendamiento.config;

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
    
    //1. Nombre del Buzón (Queue)
    public static final String QUEUE_NOTIFICACIONES = "navium.notificaciones.queue";
    //2. Enrutador inteligente (Exchange) 
    public static final String EXCHANGE_NOTIFICACIONES = "navium.exchange";
    //3. Ruta (Routing Key)
    public static final String ROUTING_KEY = "navium.agendamiento.creado";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NOTIFICACIONES, true); // true para el caso en que se apague el servidor, no se borran los mensajes
    }

    @Bean TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NOTIFICACIONES);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
}
