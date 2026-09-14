//package com.raymond.bookingsystem.service;
//
//import com.raymond.bookingsystem.model.Booking;
//import com.raymond.bookingsystem.model.Room;
//import com.raymond.bookingsystem.model.BookingStatus;
//import com.raymond.bookingsystem.repository.BookingRepository;
//import com.raymond.bookingsystem.repository.RoomRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class
//BookingServiceTest {
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private CustomerRepository customerRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @InjectMocks
//    private BookingService bookingService;
//
//    private Room room;
//    private Customer customer;
//
//    @BeforeEach
//    void setUp() {
//        room = new Room();
//        room.setId(1L);
//
//        customer = new Customer();
//        customer.setId(2L);
//    }
//
//    private Booking newBooking(LocalDate checkIn, LocalDate checkOut) {
//        Booking booking = new Booking();
//        booking.setCheckInDate(checkIn);
//        booking.setCheckOutDate(checkOut);
//        booking.setRoom(room);
//        return booking;
//    }
//
//    @Test
//    void createBooking_skaparBokningMedStatusACTIVEOchBekraftelsenummer() {
//        Booking booking = newBooking(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//
//        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//        when(bookingRepository.findConflictingBookings(any(), any(), any())).thenReturn(List.of());
//        when(bookingRepository.existsByBookingConfirmation(anyString())).thenReturn(false);
//        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        Booking result = bookingService.createBooking(booking, 2L);
//
//        assertThat(result.getStatus()).isEqualTo(BookingStatus.ACTIVE);
//        assertThat(result.getBookingConfirmation()).isNotBlank();
//        assertThat(result.getCustomer()).isEqualTo(customer);
//        verify(bookingRepository).save(booking);
//    }
//
//    @Test
//    void createBookingKastarFelNarUtcheckningInteArEfterIncheckning() {
//        Booking booking = newBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(3));
//
//        assertThatThrownBy(() -> bookingService.createBooking(booking, 2L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Utcheckningsdatum måste vara efter incheckningsdatum");
//
//        verify(bookingRepository, never()).save(any());
//    }
//
//    @Test
//    void createBookingKastarFelNarRummetRedanArBokat() {
//        Booking booking = newBooking(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//
//        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//        when(bookingRepository.findConflictingBookings(any(), any(), any()))
//                .thenReturn(List.of(new Booking()));
//
//        assertThatThrownBy(() -> bookingService.createBooking(booking, 2L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Rummet är redan bokat");
//
//        verify(bookingRepository, never()).save(any());
//    }
//
////    @Test
////    void cancelBookingSatterStatusCANCELLEDFrigorRummetOchRaderar() {
////        Booking booking = newBooking(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
////        booking.setStatus(BookingStatus.ACTIVE);
////        room.setAvailable(false);
////
////        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
////
////        bookingService.cancelBooking(5L);
////
////        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
////        assertThat(room.isAvailable()).isTrue();
////        verify(roomRepository, times(1)).save(room);
////        verify(bookingRepository, times(1)).delete(booking);
////    }
//
//    @Test
//    void getBookingByIdKastarFelNarBokningSaknas() {
//        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> bookingService.getBookingById(99L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Bokning hittades inte");
//    }
//}
