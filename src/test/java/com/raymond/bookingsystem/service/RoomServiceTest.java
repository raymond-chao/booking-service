package com.raymond.bookingsystem.service;

import com.raymond.bookingsystem.model.Room;
import com.raymond.bookingsystem.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void getRoomByIdKastarFelNarRummetSaknas() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rummet hittades inte");
    }

    @Test
    void getAvailableRoomsReturnerarLedigaRumForGiltigaDatum() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Room room = new Room();
        room.setId(1L);

        when(roomRepository.findAvailableRooms(checkIn, checkOut)).thenReturn(List.of(room));

        List<Room> result = roomService.getAvailableRooms(checkIn, checkOut);

        assertThat(result).containsExactly(room);
    }

    @Test
    void getAvailableRoomsKastaFelnarDatumSaknas() {
        assertThatThrownBy(() -> roomService.getAvailableRooms(null, LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("måste anges");

        verifyAldrigAnropatRepository();
    }

    @Test
    void getAvailableRoomsKastarFelNarIncheckningArEfterUtcheckning() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> roomService.getAvailableRooms(checkIn, checkOut))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utcheckningsdatum måste vara efter incheckningsdatum");

        verifyAldrigAnropatRepository();
    }

    @Test
    void getAvailableRoomsKastarFelNarIncheckningArIDetForflutna() {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> roomService.getAvailableRooms(checkIn, checkOut))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("förflutna");

        verifyAldrigAnropatRepository();
    }

    private void verifyAldrigAnropatRepository() {
        org.mockito.Mockito.verify(roomRepository, never()).findAvailableRooms(any(), any());
    }
}