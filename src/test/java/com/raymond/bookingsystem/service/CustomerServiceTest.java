//package com.raymond.bookingsystem.service;
//
//import com.raymond.bookingsystem.error.BadRequestException;
//import com.raymond.bookingsystem.error.NotFoundException;
//import com.raymond.bookingsystem.model.Booking;
//import com.raymond.bookingsystem.model.BookingStatus;
//import com.raymond.bookingsystem.repository.BookingRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CustomerServiceTest {
//
//    @Mock
//    private CustomerRepository customerRepository;
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @InjectMocks
//    private CustomerService customerService;
//
//    @Test
//    void getCustomerByIdKastarNotFoundExceptionNarKundSaknas() {
//        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> customerService.getCustomerById(99L))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessageContaining("Customer not found: 99");
//    }
//
//    @Test
//    void createCustomerHasharLosenordOchSpararKunden() {
//        CreateCustomerRequest request =
//                new CreateCustomerRequest("Anna", "anna@example.com", "0701234567", "hemligt");
//
//        when(passwordEncoder.encode("hemligt")).thenReturn("HASHAT");
//        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        Customer result = customerService.createCustomer(request);
//
//        assertThat(result.getName()).isEqualTo("Anna");
//        assertThat(result.getEmail()).isEqualTo("anna@example.com");
//        assertThat(result.getPhoneNumber()).isEqualTo("0701234567");
//        assertThat(result.getPassword()).isEqualTo("HASHAT");
//        verify(customerRepository).save(any(Customer.class));
//    }
//
//    @Test
//    void updateCustomerUppdaterarEndastAngivnaFalt() {
//        Customer existing = new Customer();
//        existing.setName("Gammalt namn");
//        existing.setEmail("gammal@example.com");
//        existing.setPhoneNumber("0700000000");
//
//        Customer updated = new Customer();
//        updated.setName("Nytt namn");
//
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
//        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        Customer result = customerService.updateCustomer(1L, updated);
//
//        assertThat(result.getName()).isEqualTo("Nytt namn");
//        assertThat(result.getEmail()).isEqualTo("gammal@example.com");
//        assertThat(result.getPhoneNumber()).isEqualTo("0700000000");
//    }
//
//    @Test
//    void deleteCustomerKastarBadRequestExceptionNarAktivaBokningarFinns() {
//        Customer customer = new Customer();
//        customer.setId(1L);
//
//        Booking activeBooking = new Booking();
//        activeBooking.setStatus(BookingStatus.ACTIVE);
//
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
//        when(bookingRepository.findAllByCustomerId(1L)).thenReturn(List.of(activeBooking));
//
//        assertThatThrownBy(() -> customerService.deleteCustomer(1L))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("aktiva bokningar");
//
//        verify(customerRepository, never()).delete(any());
//    }
//
//    @Test
//    void deleteCustomerRaderarKundNarIngaAktivaBokningarFinns() {
//        Customer customer = new Customer();
//        customer.setId(1L);
//
//        Booking cancelledBooking = new Booking();
//        cancelledBooking.setStatus(BookingStatus.CANCELLED);
//
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
//        when(bookingRepository.findAllByCustomerId(1L)).thenReturn(List.of(cancelledBooking));
//
//        customerService.deleteCustomer(1L);
//
//        verify(customerRepository, times(1)).delete(customer);
//    }
//}