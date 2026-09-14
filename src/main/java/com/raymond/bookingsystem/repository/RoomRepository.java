package com.raymond.bookingsystem.repository;

import com.raymond.bookingsystem.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Hitta rum som inte har överlappande aktiva bokningar
    @Query("""
            SELECT r FROM Room r
            WHERE r.id NOT IN (
                SELECT b.room.id FROM Booking b
                WHERE b.status = 'ACTIVE'
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            )
            """)
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut);

    // Kontrollera om ett specifikt rum är ledigt
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN false ELSE true END
            FROM Booking b
            WHERE b.room.id = :roomId
            AND b.status = 'ACTIVE'
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
            """)
    boolean isRoomAvailable(@Param("roomId") Long roomId,
                            @Param("checkIn") LocalDate checkIn,
                            @Param("checkOut") LocalDate checkOut);

    List<Room> findByBeds(int beds);
}