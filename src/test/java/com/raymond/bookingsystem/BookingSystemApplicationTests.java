package com.raymond.bookingsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "customer-service.url=http://localhost:8081")
class BookingSystemApplicationTests {

    @Test
    void contextLoads() {
    }

}
