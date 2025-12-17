package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.UtilityReadingCreationRequest;
import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.dto.UtilityReadingUpdateRequest;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.UtilityReading;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.UtilityReadingMapper;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UtilityReadingServiceTest {

    @Mock private UtilityReadingRepository utilityReadingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UtilityReadingMapper utilityReadingMapper;

    @InjectMocks private UtilityReadingService utilityReadingService;

    private Room room;

    @BeforeEach
    void setUp() {
        final var building = new Building();
        building.setId(1);

        room = new Room();
        room.setId(10);
        room.setRoomNo("P.101");
        room.setBuilding(building);

        lenient()
                .when(utilityReadingMapper.toResponse(any(UtilityReading.class)))
                .thenAnswer(
                        invocation -> {
                            final var r = invocation.getArgument(0, UtilityReading.class);
                            final var res = new UtilityReadingResponse();
                            res.setId(r.getId());
                            res.setRoomId(r.getRoom() != null ? r.getRoom().getId() : null);
                            res.setRoomNo(r.getRoom() != null ? r.getRoom().getRoomNo() : null);
                            res.setMonth(r.getMonth());
                            res.setElectricIndex(r.getElectricIndex());
                            res.setWaterIndex(r.getWaterIndex());
                            res.setImageEvidence(r.getImageEvidence());
                            return res;
                        });
    }

    @Test
    void createUtilityReading_roomNotFound_shouldThrow() {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(999);
        req.setMonth("2025-01");

        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        final var ex =
                assertThrows(
                        AppException.class, () -> utilityReadingService.createUtilityReading(req));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void createUtilityReading_existingReading_shouldThrow() {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-01");

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(new UtilityReading()));

        final var ex =
                assertThrows(
                        AppException.class, () -> utilityReadingService.createUtilityReading(req));
        assertEquals(ErrorCode.UTILITY_READING_EXISTED, ex.getErrorCode());
    }

    @Test
    void createUtilityReading_whenNewIndexLessThanPrevious_shouldThrow() {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(90);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());

        final var prev = new UtilityReading();
        prev.setRoom(room);
        prev.setMonth("2025-01");
        prev.setElectricIndex(100);
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prev));

        final var ex =
                assertThrows(
                        AppException.class, () -> utilityReadingService.createUtilityReading(req));
        assertEquals(ErrorCode.UTILITY_READING_INVALID_INDEX, ex.getErrorCode());
    }

    @Test
    void createUtilityReading_whenMeterReset_shouldAllowNewIndexLessThanPrevious() {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(10); // < previous
        req.setIsMeterReset(true);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());

        final var prev = new UtilityReading();
        prev.setRoom(room);
        prev.setMonth("2025-01");
        prev.setElectricIndex(100);
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prev));

        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        invocation -> {
                            final var r = invocation.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(10, res.getElectricIndex());
        verify(utilityReadingRepository).save(any(UtilityReading.class));
    }

    @Test
    void createUtilityReading_success_shouldSaveAndMap() {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(120);
        req.setWaterIndex(50);
        req.setImageEvidence("http://img");

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty());

        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        invocation -> {
                            final var r = invocation.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(1, res.getId());
        assertEquals(room.getId(), res.getRoomId());
        assertEquals("2025-02", res.getMonth());
        verify(utilityReadingRepository).save(any(UtilityReading.class));
    }

    @Test
    void updateUtilityReading_notFound_shouldThrow() {
        when(utilityReadingRepository.findById(99)).thenReturn(Optional.empty());
        final var ex =
                assertThrows(
                        AppException.class,
                        () ->
                                utilityReadingService.updateUtilityReading(
                                        99, new UtilityReadingUpdateRequest()));
        assertEquals(ErrorCode.UTILITY_READING_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateUtilityReading_whenNewWaterIndexLessThanPrevious_shouldThrow() {
        final var reading = new UtilityReading();
        reading.setId(5);
        reading.setRoom(room);
        reading.setMonth("2025-02");
        reading.setWaterIndex(200);

        when(utilityReadingRepository.findById(5)).thenReturn(Optional.of(reading));

        final var prev = new UtilityReading();
        prev.setRoom(room);
        prev.setMonth("2025-01");
        prev.setWaterIndex(300);
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prev));

        final var req = new UtilityReadingUpdateRequest();
        req.setWaterIndex(250); // < 300

        final var ex =
                assertThrows(
                        AppException.class,
                        () -> utilityReadingService.updateUtilityReading(5, req));
        assertEquals(ErrorCode.UTILITY_READING_INVALID_INDEX, ex.getErrorCode());
    }

    @Test
    void updateUtilityReading_whenMeterReset_shouldAllowNewIndexLessThanPrevious() {
        final var reading = new UtilityReading();
        reading.setId(5);
        reading.setRoom(room);
        reading.setMonth("2025-02");
        reading.setWaterIndex(200);

        when(utilityReadingRepository.findById(5)).thenReturn(Optional.of(reading));

        final var prev = new UtilityReading();
        prev.setRoom(room);
        prev.setMonth("2025-01");
        prev.setWaterIndex(300);
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prev));

        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final var req = new UtilityReadingUpdateRequest();
        req.setWaterIndex(250); // < 300
        req.setIsMeterReset(true);

        final var res = utilityReadingService.updateUtilityReading(5, req);
        assertNotNull(res);
        assertEquals(250, res.getWaterIndex());
        verify(utilityReadingRepository).save(eq(reading));
    }

    @Test
    void updateUtilityReading_success_partialUpdate_shouldSave() {
        final var reading = new UtilityReading();
        reading.setId(5);
        reading.setRoom(room);
        reading.setMonth("2025-02");
        reading.setElectricIndex(100);
        reading.setWaterIndex(200);

        when(utilityReadingRepository.findById(5)).thenReturn(Optional.of(reading));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty());

        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final var req = new UtilityReadingUpdateRequest();
        req.setElectricIndex(150);
        // waterIndex null -> keep old

        final var res = utilityReadingService.updateUtilityReading(5, req);
        assertNotNull(res);
        assertEquals(150, res.getElectricIndex());
        assertEquals(200, res.getWaterIndex());
        verify(utilityReadingRepository).save(eq(reading));
    }

    @Test
    void getUtilityReadingById_notFound_shouldThrow() {
        when(utilityReadingRepository.findById(1)).thenReturn(Optional.empty());
        final var ex =
                assertThrows(
                        AppException.class, () -> utilityReadingService.getUtilityReadingById(1));
        assertEquals(ErrorCode.UTILITY_READING_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getUtilityReadingsByRoomId_shouldMapList() {
        final var r1 = new UtilityReading();
        r1.setId(1);
        r1.setRoom(room);
        r1.setMonth("2025-02");
        final var r2 = new UtilityReading();
        r2.setId(2);
        r2.setRoom(room);
        r2.setMonth("2025-01");

        when(utilityReadingRepository.findByRoomIdOrderByMonthDesc(room.getId()))
                .thenReturn(List.of(r1, r2));

        final var res = utilityReadingService.getUtilityReadingsByRoomId(room.getId());
        assertEquals(2, res.size());
        assertEquals(1, res.get(0).getId());
        assertEquals(2, res.get(1).getId());
    }

    @Test
    void getUtilityReadingsByBuildingAndMonth_shouldMapList() {
        final var r1 = new UtilityReading();
        r1.setId(1);
        r1.setRoom(room);
        r1.setMonth("2025-01");

        when(utilityReadingRepository.findByRoomBuildingIdAndMonth(1, "2025-01"))
                .thenReturn(List.of(r1));

        final var res = utilityReadingService.getUtilityReadingsByBuildingAndMonth(1, "2025-01");
        assertEquals(1, res.size());
        assertEquals(1, res.get(0).getId());
    }

    // ===== Additional tests for better branch coverage =====

    @Test
    void getUtilityReadingById_found_shouldReturnResponse() {
        final var reading = new UtilityReading();
        reading.setId(1);
        reading.setRoom(room);
        reading.setMonth("2025-01");
        reading.setElectricIndex(100);
        reading.setWaterIndex(50);

        when(utilityReadingRepository.findById(1)).thenReturn(Optional.of(reading));

        final var res = utilityReadingService.getUtilityReadingById(1);
        assertNotNull(res);
        assertEquals(1, res.getId());
        assertEquals(room.getId(), res.getRoomId());
    }

    @Test
    void createUtilityReading_whenJanuaryPeriod_shouldUseDecemberOfPreviousYear() {
        // Testing getPreviousMonth for January -> December transition
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-01");
        req.setElectricIndex(150);
        req.setWaterIndex(60);

        final var prevReading = new UtilityReading();
        prevReading.setRoom(room);
        prevReading.setMonth("2024-12");
        prevReading.setElectricIndex(100);
        prevReading.setWaterIndex(50);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2024-12"))
                .thenReturn(Optional.of(prevReading));
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(150, res.getElectricIndex());
    }

    @Test
    void createUtilityReading_whenInvalidPeriodFormat_shouldHandleGracefully() {
        // Testing getPreviousMonth exception handling
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("invalid-period");
        req.setElectricIndex(100);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "invalid-period"))
                .thenReturn(Optional.empty());
        // When getPreviousMonth fails, it returns "" and findByRoomIdAndMonth will return empty
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), ""))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
    }

    @Test
    void createUtilityReading_whenPreviousElectricIndexNull_shouldNotThrow() {
        // Branch: previousElectricIndex is null
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(
                90); // Even though this is less than some value, previousElectricIndex is null

        final var prevReading = new UtilityReading();
        prevReading.setRoom(room);
        prevReading.setMonth("2025-01");
        prevReading.setElectricIndex(null); // null - should skip validation

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prevReading));
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(90, res.getElectricIndex());
    }

    @Test
    void createUtilityReading_whenPreviousWaterIndexNull_shouldNotThrow() {
        // Branch: previousWaterIndex is null
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setWaterIndex(30); // Even though this might be less, previousWaterIndex is null

        final var prevReading = new UtilityReading();
        prevReading.setRoom(room);
        prevReading.setMonth("2025-01");
        prevReading.setWaterIndex(null); // null - should skip validation

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prevReading));
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(30, res.getWaterIndex());
    }

    @Test
    void createUtilityReading_whenNewElectricIndexNull_shouldNotValidate() {
        // Branch: electricIndex is null - skip validation
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(null);
        req.setWaterIndex(60);

        final var prevReading = new UtilityReading();
        prevReading.setRoom(room);
        prevReading.setMonth("2025-01");
        prevReading.setElectricIndex(100);
        prevReading.setWaterIndex(50);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prevReading));
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
    }

    @Test
    void createUtilityReading_whenNewWaterIndexNull_shouldNotValidate() {
        // Branch: waterIndex is null - skip validation
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(120);
        req.setWaterIndex(null);

        final var prevReading = new UtilityReading();
        prevReading.setRoom(room);
        prevReading.setMonth("2025-01");
        prevReading.setElectricIndex(100);
        prevReading.setWaterIndex(50);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.of(prevReading));
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
    }

    @Test
    void createUtilityReading_whenNoPreviousReading_shouldNotValidate() {
        // Branch: previousReading is empty
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(room.getId());
        req.setMonth("2025-02");
        req.setElectricIndex(50); // Even if this is low, no previous reading exists
        req.setWaterIndex(30);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-02"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty()); // No previous reading

        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(
                        inv -> {
                            final var r = inv.getArgument(0, UtilityReading.class);
                            r.setId(1);
                            return r;
                        });

        final var res = utilityReadingService.createUtilityReading(req);
        assertNotNull(res);
        assertEquals(50, res.getElectricIndex());
    }

    @Test
    void updateUtilityReading_withImageEvidence_shouldUpdate() {
        // Testing imageEvidence update branch
        final var reading = new UtilityReading();
        reading.setId(5);
        reading.setRoom(room);
        reading.setMonth("2025-02");
        reading.setElectricIndex(100);
        reading.setWaterIndex(200);
        reading.setImageEvidence("old-image.jpg");

        when(utilityReadingRepository.findById(5)).thenReturn(Optional.of(reading));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final var req = new UtilityReadingUpdateRequest();
        req.setImageEvidence("new-image.jpg");
        // No electric/water index update

        final var res = utilityReadingService.updateUtilityReading(5, req);
        assertNotNull(res);
        assertEquals("new-image.jpg", res.getImageEvidence());
    }

    @Test
    void updateUtilityReading_withWaterIndexOnly_shouldUpdate() {
        final var reading = new UtilityReading();
        reading.setId(5);
        reading.setRoom(room);
        reading.setMonth("2025-02");
        reading.setElectricIndex(100);
        reading.setWaterIndex(200);

        when(utilityReadingRepository.findById(5)).thenReturn(Optional.of(reading));
        when(utilityReadingRepository.findByRoomIdAndMonth(room.getId(), "2025-01"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.save(any(UtilityReading.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final var req = new UtilityReadingUpdateRequest();
        req.setWaterIndex(250);
        // electricIndex null -> keep old

        final var res = utilityReadingService.updateUtilityReading(5, req);
        assertNotNull(res);
        assertEquals(100, res.getElectricIndex()); // Unchanged
        assertEquals(250, res.getWaterIndex()); // Updated
    }

    @Test
    void getUtilityReadingsByRoomId_emptyList_shouldReturnEmpty() {
        when(utilityReadingRepository.findByRoomIdOrderByMonthDesc(room.getId()))
                .thenReturn(List.of());

        final var res = utilityReadingService.getUtilityReadingsByRoomId(room.getId());
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    void getUtilityReadingsByBuildingAndMonth_emptyList_shouldReturnEmpty() {
        when(utilityReadingRepository.findByRoomBuildingIdAndMonth(1, "2025-01"))
                .thenReturn(List.of());

        final var res = utilityReadingService.getUtilityReadingsByBuildingAndMonth(1, "2025-01");
        assertNotNull(res);
        assertEquals(0, res.size());
    }
}
