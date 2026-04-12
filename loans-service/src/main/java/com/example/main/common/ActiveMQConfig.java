package com.example.main.common;

import jakarta.jms.ConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class ActiveMQConfig {


    @Value("${settings.amq.broker.url}")
    private String amqUrl;

    @Value("${settings.amq.username}")
    private String amqUsername;
    @Value("${settings.amq.password}")
    private String amqPassword ;

    @Value("${settings.amq.concurrency}")
    private String amqConcurrency ;

    @Value("${settings.amq.connection.pool.cache}")
    private Integer amqConnectionPoolCache ;


//    private final SSLContext sslContext;

    public ActiveMQConfig() {

    }


    @Bean
    public ConnectionFactory connectionFactory(){

       JmsConnectionFactory connectionFactory = new JmsConnectionFactory();
        connectionFactory.setRemoteURI(amqUrl);
        connectionFactory.setPassword(amqPassword);
        connectionFactory.setUsername(amqUsername);
//        connectionFactory.setSslContext(sslContext);

        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(amqConnectionPoolCache);

        return cachingConnectionFactory;
    }


    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency(amqConcurrency);
        return factory;
    }
}

