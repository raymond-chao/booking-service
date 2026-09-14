package com.raymond.bookingsystem.controllers;

import com.raymond.bookingsystem.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;

@Controller
public class RoomWebController {

    private final RoomService roomService;

    public RoomWebController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public String showAllRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("pageTitle", "Alla Rum - Pensionat RMJ");
        return "rooms";
    }

    @GetMapping("/available-rooms")
    public String showAvailableRooms(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,

            Model model
    ) {
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("pageTitle", "Lediga Rum - Pensionat RMJ");

        if (checkIn == null || checkOut == null) {
            model.addAttribute("rooms", Collections.emptyList());
            model.addAttribute("searchPerformed", false);
        } else {
            model.addAttribute("rooms", roomService.getAvailableRooms(checkIn, checkOut));
            model.addAttribute("searchPerformed", true);
        }

        return "available-rooms";
    }
}