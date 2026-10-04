package com.raymond.bookingsystem.integration;

import com.raymond.bookingsystem.client.CustomerClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerClient customerClient;

    @Value("${jwt.secret:min-superhemliga-jwt-nyckel-som-ar-jattelang-123}")
    private String jwtSecret;

    private String bearerToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("hej@test.com")
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void skapaBokningGer201() throws Exception {

        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(15);

//        Arrange
        when(customerClient.customerExists("hej@test.com")).thenReturn(true);

//        Act and assert
        mockMvc.perform(post("/api/bookings").header("Authorization", bearerToken()).contentType(MediaType.APPLICATION_JSON)
                        //.content("{\"room\":{\"id\":1},\"checkInDate\":\"2026-10-01\",\"checkOutDate\":\"2026-10-05\",\"customerEmail\":\"hej@test.com\"}"))
                        .content("""
                                {
                                  "room": {"id": 1},
                                  "checkInDate": "%s",
                                  "checkOutDate": "%s",
                                  "customerEmail": "hej@test.com"
                                }
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isCreated());

    }

    @Test
    void dubbelBokningGer409() throws Exception {
//        Arrange
        when(customerClient.customerExists("hej@test.com")).thenReturn(true);
        when(customerClient.customerExists("da@test.com")).thenReturn(true);

        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(15);

//        Act and Assert
        mockMvc.perform(post("/api/bookings").header("Authorization", bearerToken()).contentType(MediaType.APPLICATION_JSON)
                        // .content("{\"room\":{\"id\":1},\"checkInDate\":\"2026-10-01\",\"checkOutDate\":\"2026-10-05\",\"customerEmail\":\"hej@test.com\"}"))
                        .content("""
                                {
                                  "room": {"id": 1},
                                  "checkInDate": "%s",
                                  "checkOutDate": "%s",
                                  "customerEmail": "hej@test.com"
                                }
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/bookings").header("Authorization", bearerToken()).contentType(MediaType.APPLICATION_JSON)
//                .content("{\"room\":{\"id\":1},\"checkInDate\":\"2026-10-01\",\"checkOutDate\":\"2026-10-05\",\"customerEmail\":\"da@test.com\"}"))
                        .content("""
                                {
                                  "room": {"id": 1},
                                  "checkInDate": "%s",
                                  "checkOutDate": "%s",
                                  "customerEmail": "da@test.com"
                                }
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isConflict());


    }

    @Test
    void okandKundGer404() throws Exception {
        when(customerClient.customerExists(any())).thenReturn(false);

        mockMvc.perform(post("/api/bookings").header("Authorization", bearerToken()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":{\"id\":1},\"checkInDate\":\"2026-12-01\",\"checkOutDate\":\"2026-12-05\",\"customerEmail\":\"hej@test.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void utanTokenGer401() throws Exception {
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":{\"id\":1},\"checkInDate\":\"2026-12-01\",\"checkOutDate\":\"2026-12-05\",\"customerEmail\":\"hej@test.com\"}"))
                .andExpect(status().isUnauthorized());
    }


}
