package com.raymond.bookingsystem.service;

import com.raymond.bookingsystem.model.Room;
import com.raymond.bookingsystem.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public List<Room> getAllRooms() {
        return repository.findAll();
    }


    public Room getRoomById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rummet hittades inte"));
    }
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("In- och utcheckningsdatum måste anges");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Utcheckningsdatum måste vara efter incheckningsdatum!");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Incheckningsdatum kan inte vara i det förflutna");
        }
        return repository.findAvailableRooms(checkIn, checkOut);
    }


    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return repository.isRoomAvailable(roomId, checkIn, checkOut);
    }
}