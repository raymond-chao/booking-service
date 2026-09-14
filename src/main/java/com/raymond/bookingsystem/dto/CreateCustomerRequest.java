package com.raymond.bookingsystem.dto;

public record CreateCustomerRequest(
        String name, String email,
        String phoneNumber, String password
){}
