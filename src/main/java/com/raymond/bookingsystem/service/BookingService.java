package com.raymond.bookingsystem.service;

import com.raymond.bookingsystem.client.CustomerClient;
import com.raymond.bookingsystem.error.BadRequestException;
import com.raymond.bookingsystem.error.ConflictException;
import com.raymond.bookingsystem.error.NotFoundException;
import com.raymond.bookingsystem.model.Booking;
import com.raymond.bookingsystem.model.Room;
import com.raymond.bookingsystem.repository.BookingRepository;
import com.raymond.bookingsystem.model.BookingStatus;
import com.raymond.bookingsystem.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    private final CustomerClient customerClient;

    private final RoomRepository roomRepository;
    
    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          CustomerClient customerClient) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.customerClient = customerClient;
    }

    //Uppdaterad booking med customer
    public Booking createBooking(Booking booking, String email) {


        if (!booking.getCheckOutDate().isAfter(booking.getCheckInDate())) {
            throw new BadRequestException("Utcheckningsdatum måste vara efter incheckningsdatum.");
        }
        if (!customerClient.customerExists(email)) {
            throw new NotFoundException("Kund finns inte: " + email);
        }

        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new NotFoundException("Rummet hittades inte"));

        booking.setCustomerEmail(email);
        booking.setRoom(room);

        List<Booking> conflicts =
                bookingRepository.findConflictingBookings(
                        booking.getRoom().getId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate()
                );

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Rummet redan bokat dessa datum");
        }

        booking.setBookingConfirmation(generateUniqueBookingConfirmation());

        booking.setStatus(BookingStatus.ACTIVE);
    
        return bookingRepository.save(booking);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Utcheckningsdatum måste vara efter incheckningsdatum.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Incheckningsdatum kan inte vara i det förflutna.");
        }
    }

    private void validateGuests(int numOfGuests, int beds) {
        if (numOfGuests < 1) {
            throw new RuntimeException("Antal gäster måste vara minst 1.");
        }
        if (numOfGuests > beds) {
            throw new RuntimeException("Antal gäster överstiger antalet sängar i rummet.");
        }
    }

    private String generateUniqueBookingConfirmation() {
        String confirmationNumber;

        do {
            confirmationNumber = String.valueOf(
                    ThreadLocalRandom.current().nextLong(100000, 1000000)
            );
        } while (bookingRepository.existsByBookingConfirmation(confirmationNumber));

        return confirmationNumber;
    }

    @Transactional
    public Booking updateBooking(Long id, Booking updatedBooking) {

        if (!updatedBooking.getCheckOutDate().isAfter(updatedBooking.getCheckInDate())) {
            throw new BadRequestException("Utcheckningsdatum måste vara efter incheckningsdatum.");
        }

        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bokning hittades inte"));

        Room room = roomRepository.findById(updatedBooking.getRoom().getId())
                .orElseThrow(() -> new NotFoundException("Rummet hittades inte"));

        validateGuests(updatedBooking.getNumOfGuests(), room.getBeds());

        List<Booking> conflicts =
                bookingRepository.findConflictingBookings(
                        room.getId(),
                        updatedBooking.getCheckInDate(),
                        updatedBooking.getCheckOutDate()
                );

        boolean hasOtherConflicts = conflicts.stream()
                .anyMatch(b -> !b.getId().equals(id));

        if (hasOtherConflicts) {
            throw new ConflictException("Datumkonflikt. Rummet är redan bokat under valda datum.");
        }

        existing.setCheckInDate(updatedBooking.getCheckInDate());
        existing.setCheckOutDate(updatedBooking.getCheckOutDate());
        existing.setNumOfGuests(updatedBooking.getNumOfGuests());
        existing.setRoom(room);

        return bookingRepository.save(existing);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bokning hittades inte"));
    }
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Bokning hittades inte"));

        bookingRepository.delete(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public boolean hasActiveBookings(String email) {
        return bookingRepository.existsByCustomerEmailAndStatus(email, BookingStatus.ACTIVE );
    }

    public List<Booking> getBookingByEmail(String email) {
        return bookingRepository.findAllByCustomerEmail(email);
    }
}