package com.raymond.bookingsystem.controllers;

import com.raymond.bookingsystem.client.CustomerClient;
import com.raymond.bookingsystem.dto.CreateCustomerRequest;
import com.raymond.bookingsystem.dto.CustomerDTO;
import com.raymond.bookingsystem.error.ConflictException;
import com.raymond.bookingsystem.model.Booking;

import com.raymond.bookingsystem.model.Room;
import com.raymond.bookingsystem.service.BookingService;
import com.raymond.bookingsystem.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class BookingWebController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final CustomerClient customerClient;



    public BookingWebController(
            BookingService bookingService,
            RoomService roomService,
            CustomerClient customerClient
    ) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.customerClient = customerClient;
    }

    @GetMapping("/book-room")
    public String showBookingForm(
            @RequestParam Long roomId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,

            Model model
    ) {
        Room room = roomService.getRoomById(roomId);

        model.addAttribute("room", room);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("pageTitle", "Boka rum " + room.getRoomNumber());

        return "booking-form";
    }

    @GetMapping("/book-room/check-availability")
    @ResponseBody
    public Map<String, Object> checkRoomAvailability(
            @RequestParam Long roomId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut
    ) {
        if (!checkOut.isAfter(checkIn)) {
            return Map.of(
                    "available", false,
                    "message", "Utcheckningsdatum måste vara efter incheckningsdatum."
            );
        }

        boolean available = roomService.isRoomAvailable(roomId, checkIn, checkOut);

        if (available) {
            return Map.of(
                    "available", true,
                    "message", "Rummet är ledigt valda datum."
            );
        }

        return Map.of(
                "available", false,
                "message", "Rummet är redan bokat dessa datum. Välj andra datum."
        );
    }

    @PostMapping("/book-room")
    public String submitBooking(
            @RequestParam Long roomId,
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numOfGuests,
            Model model
    ) {
        Room room = roomService.getRoomById(roomId);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        //booking.setNumOfGuests(numOfGuests);

        Booking savedBooking = bookingService.createBooking(booking, email);

        model.addAttribute("booking", savedBooking);
        model.addAttribute("room", room);
        model.addAttribute("pageTitle", "Bokning bekräftad");

        return "booking-confirmation";
    }

    @GetMapping("/bookings/edit/{id}")
    public String showEditBookingForm(
            @PathVariable Long id,
            Model model
    ) {
        Booking booking = bookingService.getBookingById(id);

        model.addAttribute("booking", booking);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("pageTitle", "Ändra bokning");

        return "booking-edit";
    }

    @PostMapping("/bookings/update/{id}")
    public String updateBooking(
            @PathVariable Long id,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            Model model
    ) {
        try {
            Room room = roomService.getRoomById(roomId);

            Booking updatedBooking = new Booking();
            updatedBooking.setRoom(room);
            updatedBooking.setCheckInDate(checkInDate);
            updatedBooking.setCheckOutDate(checkOutDate);

            Booking savedBooking = bookingService.updateBooking(id, updatedBooking);

            model.addAttribute("booking", savedBooking);
            model.addAttribute("success", "Bokningen har uppdaterats.");
            model.addAttribute("pageTitle", "Din bokning");

            return "booking-details";
        } catch (RuntimeException e) {
            Booking booking = bookingService.getBookingById(id);

            model.addAttribute("booking", booking);
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Ändra bokning");

            return "booking-edit";
        }
    }

    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(
            @PathVariable Long id,
            Model model
    ) {
        bookingService.cancelBooking(id);

        model.addAttribute("success", "Bokningen har avbokats.");
        model.addAttribute("pageTitle", "Bokning avbokad");

        return "booking-cancelled";
    }

    @GetMapping("/account")
    public String account(@RequestParam (required = false) String email, Model model) {
        if (email == null || email.isBlank()) {
            return "account";
        }
        CustomerDTO customer = customerClient.findByEmail(email);
        List<Booking> bookings = bookingService.getBookingByEmail(email);
        model.addAttribute("customer", customer);
        model.addAttribute("bookings", bookings);
        return "account";
    }

    @GetMapping("/customers/new")
    public String showRegisterForm() {
        return "customer-form";
    }

    @PostMapping("/customers/new")
    public String saveCustomer(@ModelAttribute CreateCustomerRequest request) {
        customerClient.createCustomer(request);
        return "redirect:/rooms";

    }
    @PostMapping("/account/delete")
    public String deleteAccount(@RequestParam String email, Model model) {
        try {
            CustomerDTO customer = customerClient.findByEmail(email);
            customerClient.deleteCustomer(customer.id());
            return "redirect:/rooms";
        } catch (ConflictException e) {
            model.addAttribute("customer", customerClient.findByEmail(email));
            model.addAttribute("bookings", bookingService.getBookingByEmail(email));
            model.addAttribute("error", e.getMessage());
            return "account";
        }
    }

}
