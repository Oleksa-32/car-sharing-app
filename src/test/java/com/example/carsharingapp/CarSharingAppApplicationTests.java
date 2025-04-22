package com.example.carsharingapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}"
})
class CarSharingAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
