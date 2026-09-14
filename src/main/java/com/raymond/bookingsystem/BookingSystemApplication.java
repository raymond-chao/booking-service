package com.raymond.bookingsystem;

import com.raymond.bookingsystem.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
public class BookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingSystemApplication.class, args);
    }

    @Bean
   @Order(2)
   CommandLineRunner runner(RoomRepository roomRepository) {
        return args -> {
            System.out.println("Antal rum i DB: " + roomRepository.count());
        };
    }
}