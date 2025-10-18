package ru.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate rt = new RetryTemplate();

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(3000L);
        rt.setBackOffPolicy(backOff);

        MaxAttemptsRetryPolicy policy = new MaxAttemptsRetryPolicy();
        policy.setMaxAttempts(3);
        rt.setRetryPolicy(policy);

        return rt;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

