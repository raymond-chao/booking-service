package com.raymond.bookingsystem.health;

import com.raymond.bookingsystem.client.CustomerClient;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomerServiceHealthIndicator implements HealthIndicator {

    private final CustomerClient  customerClient;

    @Override
    public @Nullable Health health() {
        try {
            customerClient.healthCheck();
            return Health.up().build();

        }catch (Exception ex){
            return Health.down().build();
        }
    }

    public CustomerServiceHealthIndicator(CustomerClient  customerClient) {
        this.customerClient = customerClient;
    }
}
