package com.raymond.bookingsystem.client;

import com.raymond.bookingsystem.dto.CreateCustomerRequest;
import com.raymond.bookingsystem.dto.CustomerDTO;
import com.raymond.bookingsystem.error.ConflictException;
import com.raymond.bookingsystem.error.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.*;

@Component
public class CustomerClient {

    private final RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(CustomerClient.class);

    public CustomerClient(@Value("${customer-service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void healthCheck(){
        try {
            restClient.get()
                    .uri("/api/customers")
                    .retrieve()
                    .toBodilessEntity();
        }catch (RestClientException e){
            throw new ServiceUnavailableException("Customer service unavailable");
        }
    }

    public boolean customerExists(String email) {
        try {
            restClient.get()
                    .uri("/api/customers/email/{email}", email)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (HttpClientErrorException e) {
            return false;

        } catch (RestClientException e) {
            log.warn("Customer service is not available while checking customer");
            throw new ServiceUnavailableException(
                    "Kundtjänst inte tillgänglig, försök igen senare"
            );
        }
    }

    public CustomerDTO findByEmail(String email) {
        try {
            return restClient.get()
                    .uri("/api/customers/email/{email}", email)
                    .retrieve()
                    .body(CustomerDTO.class);

        } catch (RestClientException e) {
            log.warn("Customer service is not available while retrieving customer");
            throw new ServiceUnavailableException(
                    "Kundtjänsten är inte tillgänglig, försök igen senare."
            );
        }
    }

    public void createCustomer(CreateCustomerRequest request) {
        try {
            restClient.post()
                    .uri("/api/customers")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            log.warn("Customer service is not available while creating customer");
            throw new ServiceUnavailableException(
                    "Kundtjänsten är inte tillgänglig, försök igen senare"
            );
        }
    }
    public void deleteCustomer(Long id){
        try {
            restClient.delete().uri("/api/customers/{id}", id).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e){
            throw new ConflictException("Kunden har aktiva bokningar och kan inte tas bort");

        } catch (RestClientException e) {
            log.warn("Customer service is not available while deleting customer");
            throw new ServiceUnavailableException("Kundtjänsten är inte tillgänlig, försök igen senare");

        }
    }
}