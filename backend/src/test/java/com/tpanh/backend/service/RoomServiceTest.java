package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.dto.RoomUpdateRequest;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.RoomMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.RoomRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    private static final int BUILDING_ID = 1;
    private static final int ROOM_ID = 1;
    private static final String BUILDING_NAME = "Trọ Xanh";
    private static final String ROOM_NO = "P.101";
    private static final int ROOM_PRICE = 3000000;
    private static final String STATUS_VACANT = "VACANT";
    private static final String STATUS_OCCUPIED = "OCCUPIED";

    @Mock private RoomRepository roomRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private RoomMapper roomMapper;

    @InjectMocks private RoomService roomService;

    private Building building;
    private Room room;

    @BeforeEach
    void setUp() {
        building = new Building();
        building.setId(BUILDING_ID);
        building.setName(BUILDING_NAME);

        room = new Room();
        room.setId(ROOM_ID);
        room.setBuilding(building);
        room.setRoomNo(ROOM_NO);
        room.setPrice(ROOM_PRICE);
        room.setStatus(STATUS_VACANT);

        // Mock mapper to return response based on room (lenient for tests that don't use mapper)
        lenient()
                .when(roomMapper.toResponse(any(Room.class)))
                .thenAnswer(
                        invocation -> {
                            final Room r = invocation.getArgument(0);
                            return RoomResponse.builder()
                                    .id(r.getId())
                                    .buildingId(
                                            r.getBuilding() != null
                                                    ? r.getBuilding().getId()
                                                    : null)
                                    .buildingName(
                                            r.getBuilding() != null
                                                    ? r.getBuilding().getName()
                                                    : null)
                                    .roomNo(r.getRoomNo())
                                    .price(r.getPrice())
                                    .status(r.getStatus())
                                    .build();
                        });
    }

    @Test
    void createRoom_WithValidRequest_ShouldReturnRoomResponse() {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(BUILDING_ID);
        request.setRoomNo(ROOM_NO);
        request.setPrice(ROOM_PRICE);
        request.setStatus(STATUS_VACANT);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(building));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = roomService.createRoom(request);

        // Then
        assertNotNull(response);
        assertEquals(ROOM_ID, response.getId());
        assertEquals(BUILDING_ID, response.getBuildingId());
        assertEquals(BUILDING_NAME, response.getBuildingName());
        assertEquals(ROOM_NO, response.getRoomNo());
        assertEquals(ROOM_PRICE, response.getPrice());
        assertEquals(STATUS_VACANT, response.getStatus());
        verify(buildingRepository).findById(BUILDING_ID);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_WithNullStatus_ShouldUseDefaultVACANT() {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(BUILDING_ID);
        request.setRoomNo(ROOM_NO);
        request.setPrice(ROOM_PRICE);
        request.setStatus(null);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(building));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = roomService.createRoom(request);

        // Then
        assertNotNull(response);
        assertEquals(STATUS_VACANT, response.getStatus());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_WithInvalidBuildingId_ShouldThrowException() {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(BUILDING_ID);
        request.setRoomNo(ROOM_NO);
        request.setPrice(ROOM_PRICE);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> roomService.createRoom(request));
        assertEquals(ErrorCode.BUILDING_NOT_FOUND, exception.getErrorCode());
        verify(buildingRepository).findById(BUILDING_ID);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void updateRoom_WithValidRequest_ShouldReturnUpdatedResponse() {
        // Given
        final var request = new RoomUpdateRequest();
        request.setRoomNo("P.102");
        request.setPrice(3500000);
        request.setStatus(STATUS_OCCUPIED);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = roomService.updateRoom(ROOM_ID, request);

        // Then
        assertNotNull(response);
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoom_WithPartialFields_ShouldUpdateOnlyProvidedFields() {
        // Given
        final var request = new RoomUpdateRequest();
        request.setPrice(3500000);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = roomService.updateRoom(ROOM_ID, request);

        // Then
        assertNotNull(response);
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoom_WithNullFields_ShouldNotUpdateFields() {
        // Given
        final var request = new RoomUpdateRequest();
        request.setRoomNo(null);
        request.setPrice(null);
        request.setStatus(null);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = roomService.updateRoom(ROOM_ID, request);

        // Then
        assertNotNull(response);
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoom_WithInvalidId_ShouldThrowException() {
        // Given
        final var request = new RoomUpdateRequest();
        request.setPrice(3500000);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> roomService.updateRoom(ROOM_ID, request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void deleteRoom_WithValidId_ShouldDeleteRoom() {
        // Given
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // When
        roomService.deleteRoom(ROOM_ID);

        // Then
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_WithInvalidId_ShouldThrowException() {
        // Given
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> roomService.deleteRoom(ROOM_ID));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).findById(ROOM_ID);
        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void getRoomsByBuildingId_WithValidBuildingId_ShouldReturnRoomList() {
        // Given
        final var room2 = new Room();
        room2.setId(2);
        room2.setBuilding(building);
        room2.setRoomNo("P.102");
        room2.setPrice(3500000);
        room2.setStatus(STATUS_OCCUPIED);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(building));
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room, room2));

        // When
        final var response = roomService.getRoomsByBuildingId(BUILDING_ID);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(buildingRepository).findById(BUILDING_ID);
        verify(roomRepository).findByBuildingId(BUILDING_ID);
    }

    @Test
    void getRoomsByBuildingId_WithInvalidBuildingId_ShouldThrowException() {
        // Given
        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> roomService.getRoomsByBuildingId(BUILDING_ID));
        assertEquals(ErrorCode.BUILDING_NOT_FOUND, exception.getErrorCode());
        verify(buildingRepository).findById(BUILDING_ID);
        verify(roomRepository, never()).findByBuildingId(any(Integer.class));
    }
}
