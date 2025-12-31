package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.MeterRecord;
import com.tpanh.backend.entity.PaymentLog;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.entity.UtilityReading;
import com.tpanh.backend.enums.InvoiceStatus;
import com.tpanh.backend.enums.MeterType;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.InvoiceMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.InvoiceRepository;
import com.tpanh.backend.repository.MeterRecordRepository;
import com.tpanh.backend.repository.PaymentLogRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
import com.tpanh.backend.security.CurrentUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    private static final int BUILDING_ID = 1;
    private static final int ROOM_ID_1 = 1;
    private static final int ROOM_ID_2 = 2;
    private static final int TENANT_ID = 1;
    private static final String PERIOD = "2025-01";
    private static final int ROOM_PRICE = 3000000;
    private static final int ELEC_UNIT_PRICE = 3000;
    private static final int WATER_UNIT_PRICE = 20000;
    private static final String ROOM_NO_1 = "P.101";
    private static final String ROOM_NO_2 = "P.102";
    private static final String TENANT_NAME = "Nguyễn Văn A";

    @Mock private InvoiceMapper invoiceMapper;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private MeterRecordRepository meterRecordRepository;
    @Mock private UtilityReadingRepository utilityReadingRepository;
    @Mock private EmailService emailService;
    @Mock private BuildingRepository buildingRepository;
    @Mock private PaymentLogRepository paymentLogRepository;
    @Mock private CurrentUser currentUser;

    @InjectMocks private InvoiceService invoiceService;

    private Building building;
    private Room room1;
    private Room room2;
    private Tenant tenant1;
    private MeterRecord elecRecord;
    private MeterRecord waterRecord;

    @BeforeEach
    void setUp() {
        building = new Building();
        building.setId(BUILDING_ID);
        building.setName("Trọ Xanh");
        building.setElecUnitPrice(ELEC_UNIT_PRICE);
        building.setWaterUnitPrice(WATER_UNIT_PRICE);
        building.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        room1 = new Room();
        room1.setId(ROOM_ID_1);
        room1.setBuilding(building);
        room1.setRoomNo(ROOM_NO_1);
        room1.setPrice(ROOM_PRICE);
        room1.setStatus(RoomStatus.OCCUPIED);

        room2 = new Room();
        room2.setId(ROOM_ID_2);
        room2.setBuilding(building);
        room2.setRoomNo(ROOM_NO_2);
        room2.setPrice(ROOM_PRICE);
        room2.setStatus(RoomStatus.OCCUPIED);

        tenant1 = new Tenant();
        tenant1.setId(TENANT_ID);
        tenant1.setRoom(room1);
        tenant1.setName(TENANT_NAME);
        tenant1.setIsContractHolder(true);

        elecRecord = new MeterRecord();
        elecRecord.setRoom(room1);
        elecRecord.setType(MeterType.ELEC);
        elecRecord.setPeriod(PERIOD);
        elecRecord.setPreviousValue(100);
        elecRecord.setCurrentValue(150);

        waterRecord = new MeterRecord();
        waterRecord.setRoom(room1);
        waterRecord.setType(MeterType.WATER);
        waterRecord.setPeriod(PERIOD);
        waterRecord.setPreviousValue(50);
        waterRecord.setCurrentValue(60);

        // Mock mapper
        lenient()
                .when(invoiceMapper.toResponse(any(Invoice.class)))
                .thenAnswer(
                        invocation -> {
                            final Invoice invoice = invocation.getArgument(0);
                            final InvoiceResponse response = new InvoiceResponse();
                            response.setId(invoice.getId());
                            response.setRoomNo(
                                    invoice.getRoom() != null
                                            ? invoice.getRoom().getRoomNo()
                                            : null);
                            response.setTenantName(
                                    invoice.getTenant() != null
                                            ? invoice.getTenant().getName()
                                            : null);
                            response.setPeriod(invoice.getPeriod());
                            response.setRoomPrice(invoice.getRoomPrice());
                            response.setElecAmount(invoice.getElecAmount());
                            response.setWaterAmount(invoice.getWaterAmount());
                            response.setTotalAmount(invoice.getTotalAmount());
                            response.setStatus(invoice.getStatus());
                            response.setDueDate(invoice.getDueDate());
                            return response;
                        });

        // Mock UtilityReadingRepository to return empty (fallback to MeterRecord for existing
        // tests)
        lenient()
                .when(utilityReadingRepository.findByRoomIdAndMonth(anyInt(), any(String.class)))
                .thenReturn(Optional.empty());
        lenient()
                .when(
                        utilityReadingRepository.existsByRoomIdAndMonthLessThan(
                                anyInt(), anyString()))
                .thenReturn(false);

        lenient().when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(building));
    }

    @Test
    void createInvoice_WithValidData_ShouldCreateInvoice() {
        // Given
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000); // (150-100) * 3000
        savedInvoice.setWaterAmount(200000); // (60-50) * 20000
        savedInvoice.setTotalAmount(3350000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);
        savedInvoice.setDueDate(LocalDate.now().plusDays(5));

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        final InvoiceResponse response = result.get(0);
        assertEquals(ROOM_NO_1, response.getRoomNo());
        assertEquals(TENANT_NAME, response.getTenantName());
        assertEquals(PERIOD, response.getPeriod());
        assertEquals(ROOM_PRICE, response.getRoomPrice());
        assertEquals(150000, response.getElecAmount());
        assertEquals(200000, response.getWaterAmount());
        assertEquals(3350000, response.getTotalAmount());
        assertEquals(InvoiceStatus.DRAFT, response.getStatus());
        assertNotNull(response.getDueDate());

        verify(roomRepository).findByBuildingId(BUILDING_ID);
        verify(invoiceRepository).existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD);
        verify(tenantRepository).findByRoomIdAndIsContractHolderTrue(ROOM_ID_1);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithExistingInvoice_ShouldSkipRoom() {
        // Given
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(true);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceRepository).existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD);
        verify(tenantRepository, never()).findByRoomIdAndIsContractHolderTrue(anyInt());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithNoTenant_ShouldSkipRoom() {
        // Given
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.empty());

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository).findByRoomIdAndIsContractHolderTrue(ROOM_ID_1);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithNoElectricityRecord_ShouldSetElecAmountToZero() {
        // Given
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.empty());
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(0);
        savedInvoice.setWaterAmount(200000);
        savedInvoice.setTotalAmount(3200000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getElecAmount());
        assertEquals(200000, result.get(0).getWaterAmount());
        assertEquals(3200000, result.get(0).getTotalAmount());
    }

    @Test
    void createInvoice_WithPerCapitaWaterMethod_ShouldCalculateCorrectly() {
        // Given
        building.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(2); // 2 tenants

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(40000); // 2 * 20000
        savedInvoice.setTotalAmount(3340000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(40000, result.get(0).getWaterAmount()); // PER_CAPITA: 2 tenants * 20000
        assertEquals(3340000, result.get(0).getTotalAmount());
    }

    @Test
    void createInvoice_WithNoWaterCalcMethod_ShouldSetWaterAmountToZero() {
        // Given
        building.setWaterCalcMethod(null);
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(0);
        savedInvoice.setTotalAmount(3150000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getWaterAmount());
        assertEquals(3150000, result.get(0).getTotalAmount());
    }

    @Test
    void createInvoice_WithMultipleRooms_ShouldCreateMultipleInvoices() {
        // Given
        final Tenant tenant2 = new Tenant();
        tenant2.setId(2);
        tenant2.setRoom(room2);
        tenant2.setName("Nguyễn Văn B");
        tenant2.setIsContractHolder(true);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1, room2));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_2, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_2))
                .thenReturn(Optional.of(tenant2));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_2, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.empty());
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_2, PERIOD, MeterType.WATER))
                .thenReturn(Optional.empty());
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);
        when(tenantRepository.countByRoomId(ROOM_ID_2)).thenReturn(1);

        final Invoice invoice1 = new Invoice();
        invoice1.setId(1);
        invoice1.setRoom(room1);
        invoice1.setTenant(tenant1);
        invoice1.setPeriod(PERIOD);
        invoice1.setRoomPrice(ROOM_PRICE);
        invoice1.setElecAmount(150000);
        invoice1.setWaterAmount(200000);
        invoice1.setTotalAmount(3350000);
        invoice1.setStatus(InvoiceStatus.DRAFT);

        final Invoice invoice2 = new Invoice();
        invoice2.setId(2);
        invoice2.setRoom(room2);
        invoice2.setTenant(tenant2);
        invoice2.setPeriod(PERIOD);
        invoice2.setRoomPrice(ROOM_PRICE);
        invoice2.setElecAmount(0);
        invoice2.setWaterAmount(0);
        invoice2.setTotalAmount(ROOM_PRICE);
        invoice2.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice1).thenReturn(invoice2);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository, org.mockito.Mockito.times(2)).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithNoRooms_ShouldReturnEmptyList() {
        // Given
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Collections.emptyList());

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithNoWaterRecord_ShouldCalculateWaterByMeterAsZero() {
        // Given
        building.setWaterCalcMethod(WaterCalcMethod.BY_METER);
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.empty());
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(0); // No water record, usage = 0
        savedInvoice.setTotalAmount(3150000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getWaterAmount());
    }

    // ===== Tests for getInvoices =====

    @Test
    void getInvoices_WithFilters_ShouldReturnPageResponse() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setRoomPrice(ROOM_PRICE);
        invoice.setElecAmount(150000);
        invoice.setWaterAmount(200000);
        invoice.setTotalAmount(3350000);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setDueDate(LocalDate.now().plusDays(5));

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Invoice> page = new PageImpl<>(Arrays.asList(invoice), pageable, 1);

        when(invoiceRepository.findByBuildingIdWithFilters(
                        BUILDING_ID, PERIOD, InvoiceStatus.UNPAID, pageable))
                .thenReturn(page);

        // When
        final var result =
                invoiceService.getInvoices(BUILDING_ID, PERIOD, InvoiceStatus.UNPAID, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertNotNull(result.getPage());
        assertEquals(0, result.getPage().getPage());
        assertEquals(10, result.getPage().getSize());
        assertEquals(1, result.getPage().getTotalElements());
        assertEquals("Lấy danh sách hóa đơn thành công", result.getMessage());
        verify(invoiceRepository)
                .findByBuildingIdWithFilters(BUILDING_ID, PERIOD, InvoiceStatus.UNPAID, pageable);
    }

    @Test
    void getInvoices_WithNullFilters_ShouldReturnAllInvoices() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Invoice> page = new PageImpl<>(Arrays.asList(invoice), pageable, 1);

        when(invoiceRepository.findByBuildingIdWithFilters(BUILDING_ID, null, null, pageable))
                .thenReturn(page);

        // When
        final var result = invoiceService.getInvoices(BUILDING_ID, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ===== Tests for getInvoiceDetail =====

    @Test
    void getInvoiceDetail_WithValidId_ShouldReturnDetailResponse() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setRoomPrice(ROOM_PRICE);
        invoice.setElecAmount(150000);
        invoice.setWaterAmount(200000);
        invoice.setTotalAmount(3350000);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final InvoiceDetailResponse detailResponse = new InvoiceDetailResponse();
        detailResponse.setId(1);
        detailResponse.setRoomNo(ROOM_NO_1);
        detailResponse.setPeriod(PERIOD);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDetailResponse(invoice)).thenReturn(detailResponse);

        // When
        final var result = invoiceService.getInvoiceDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ROOM_NO_1, result.getRoomNo());
        verify(invoiceRepository).findById(1);
    }

    @Test
    void getInvoiceDetail_WithInvalidId_ShouldThrowException() {
        // Given
        when(invoiceRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.getInvoiceDetail(999));
        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getInvoiceDetail_WithUtilityReading_ShouldReturnMeterDetails() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final InvoiceDetailResponse detailResponse = new InvoiceDetailResponse();
        detailResponse.setId(1);
        detailResponse.setRoomNo(ROOM_NO_1);

        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(150);
        currentReading.setWaterIndex(60);

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12");
        previousReading.setElectricIndex(100);
        previousReading.setWaterIndex(50);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDetailResponse(invoice)).thenReturn(detailResponse);
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));

        // When
        final var result = invoiceService.getInvoiceDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getElecPreviousValue());
        assertEquals(150, result.getElecCurrentValue());
        assertEquals(50, result.getElecUsage());
        assertEquals(50, result.getWaterPreviousValue());
        assertEquals(60, result.getWaterCurrentValue());
        assertEquals(10, result.getWaterUsage());
    }

    @Test
    void
            getInvoiceDetail_WithUtilityReadingMissingPreviousButHasHistory_ShouldReturnNullPreviousValues() {
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final InvoiceDetailResponse detailResponse = new InvoiceDetailResponse();
        detailResponse.setId(1);
        detailResponse.setRoomNo(ROOM_NO_1);

        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(150);
        currentReading.setWaterIndex(60);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDetailResponse(invoice)).thenReturn(detailResponse);
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.existsByRoomIdAndMonthLessThan(ROOM_ID_1, PERIOD))
                .thenReturn(true);

        final var result = invoiceService.getInvoiceDetail(1);
        assertNotNull(result);
        assertNull(result.getElecPreviousValue());
        assertNull(result.getElecUsage());
        assertNull(result.getWaterPreviousValue());
        assertNull(result.getWaterUsage());
    }

    @Test
    void getInvoiceDetail_WithMeterRecord_ShouldReturnMeterDetails() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final InvoiceDetailResponse detailResponse = new InvoiceDetailResponse();
        detailResponse.setId(1);
        detailResponse.setRoomNo(ROOM_NO_1);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDetailResponse(invoice)).thenReturn(detailResponse);
        when(utilityReadingRepository.findByRoomIdAndMonth(anyInt(), anyString()))
                .thenReturn(Optional.empty());
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));

        // When
        final var result = invoiceService.getInvoiceDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getElecPreviousValue());
        assertEquals(150, result.getElecCurrentValue());
        assertEquals(50, result.getElecUsage());
        assertEquals(50, result.getWaterPreviousValue());
        assertEquals(60, result.getWaterCurrentValue());
        assertEquals(10, result.getWaterUsage());
    }

    // ===== Tests for payInvoice =====

    @Test
    void payInvoice_WithUnpaidInvoice_ShouldUpdateToPaid() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setTotalAmount(3350000);

        final Invoice paidInvoice = new Invoice();
        paidInvoice.setId(1);
        paidInvoice.setRoom(room1);
        paidInvoice.setTenant(tenant1);
        paidInvoice.setPeriod(PERIOD);
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setTotalAmount(3350000);
        paidInvoice.setPaidAt(LocalDateTime.now());

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(paidInvoice);

        // When
        final var result = invoiceService.payInvoice(1);

        // Then
        assertNotNull(result);
        assertEquals(InvoiceStatus.PAID, result.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(paymentLogRepository).save(any(PaymentLog.class));
    }

    @Test
    void payInvoice_WithDraftInvoice_ShouldUpdateToPaid() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setStatus(InvoiceStatus.DRAFT);

        final Invoice paidInvoice = new Invoice();
        paidInvoice.setId(1);
        paidInvoice.setRoom(room1);
        paidInvoice.setTenant(tenant1);
        paidInvoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(paidInvoice);

        // When
        final var result = invoiceService.payInvoice(1);

        // Then
        assertEquals(InvoiceStatus.PAID, result.getStatus());
    }

    @Test
    void payInvoice_WithInvalidId_ShouldThrowException() {
        // Given
        when(invoiceRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.payInvoice(999));
        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void payInvoice_WithAlreadyPaidInvoice_ShouldThrowException() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));

        // When & Then
        final var exception = assertThrows(AppException.class, () -> invoiceService.payInvoice(1));
        assertEquals(ErrorCode.INVOICE_ALREADY_PAID, exception.getErrorCode());
    }

    @Test
    void payInvoice_WithVoidInvoice_ShouldThrowException() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.VOID);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));

        // When & Then
        final var exception = assertThrows(AppException.class, () -> invoiceService.payInvoice(1));
        assertEquals(ErrorCode.INVOICE_CANNOT_BE_PAID, exception.getErrorCode());
    }

    @Test
    void markOverdueInvoices_ShouldMarkInvoicesAsOverdueAndLog() {
        // Given
        final Invoice invoice1 = new Invoice();
        invoice1.setId(1);
        invoice1.setStatus(InvoiceStatus.UNPAID);
        invoice1.setTotalAmount(100000);
        invoice1.setDueDate(LocalDate.now().minusDays(1));

        final Invoice invoice2 = new Invoice();
        invoice2.setId(2);
        invoice2.setStatus(InvoiceStatus.DRAFT);
        invoice2.setTotalAmount(200000);
        invoice2.setDueDate(LocalDate.now().minusDays(1));

        when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                .thenReturn(Arrays.asList(invoice1, invoice2));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        final int count = invoiceService.markOverdueInvoices();

        // Then
        assertEquals(2, count);
        assertEquals(InvoiceStatus.OVERDUE, invoice1.getStatus());
        assertEquals(InvoiceStatus.OVERDUE, invoice2.getStatus());
        verify(invoiceRepository, org.mockito.Mockito.times(2)).save(any(Invoice.class));
        verify(paymentLogRepository, org.mockito.Mockito.times(2)).save(any(PaymentLog.class));
    }

    @Test
    void markOverdueInvoices_WithNoOverdueInvoices_ShouldReturnZero() {
        // Given
        when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        final int count = invoiceService.markOverdueInvoices();

        // Then
        assertEquals(0, count);
        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(paymentLogRepository, never()).save(any(PaymentLog.class));
    }

    // ===== Tests for sendInvoiceEmail =====

    @Test
    void sendInvoiceEmail_WithValidInvoice_ShouldSendEmail() {
        // Given
        tenant1.setEmail("tenant@example.com");
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);
        invoice.setPeriod(PERIOD);
        invoice.setTotalAmount(3350000);
        invoice.setDueDate(LocalDate.now().plusDays(5));

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));
        doNothing()
                .when(emailService)
                .sendInvoiceEmail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt(),
                        any(LocalDate.class));

        // When & Then
        assertDoesNotThrow(() -> invoiceService.sendInvoiceEmail(1));
        verify(emailService)
                .sendInvoiceEmail(
                        eq("tenant@example.com"),
                        eq(TENANT_NAME),
                        eq(ROOM_NO_1),
                        eq(PERIOD),
                        eq(3350000),
                        any(LocalDate.class));
    }

    @Test
    void sendInvoiceEmail_WithInvalidInvoiceId_ShouldThrowException() {
        // Given
        when(invoiceRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.sendInvoiceEmail(999));
        assertEquals(ErrorCode.INVOICE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void sendInvoiceEmail_WithNullTenant_ShouldThrowException() {
        // Given
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(null);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.sendInvoiceEmail(1));
        assertEquals(ErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void sendInvoiceEmail_WithNullEmail_ShouldThrowException() {
        // Given
        tenant1.setEmail(null);
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.sendInvoiceEmail(1));
        assertEquals(ErrorCode.EMAIL_REQUIRED, exception.getErrorCode());
    }

    @Test
    void sendInvoiceEmail_WithBlankEmail_ShouldThrowException() {
        // Given
        tenant1.setEmail("   ");
        final Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setRoom(room1);
        invoice.setTenant(tenant1);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(invoice));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> invoiceService.sendInvoiceEmail(1));
        assertEquals(ErrorCode.EMAIL_REQUIRED, exception.getErrorCode());
    }

    // ===== Tests for createInvoice with UtilityReading =====

    @Test
    void createInvoice_WithUtilityReading_ShouldCalculateElectricityCost() {
        // Given
        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(150);
        currentReading.setWaterIndex(null);

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12");
        previousReading.setElectricIndex(100);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000); // (150-100) * 3000
        savedInvoice.setWaterAmount(200000);
        savedInvoice.setTotalAmount(3350000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(150000, result.get(0).getElecAmount());
    }

    @Test
    void createInvoice_WithUtilityReadingMissingPreviousButHasHistory_ShouldThrow() {
        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(150);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));

        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.empty());
        when(utilityReadingRepository.existsByRoomIdAndMonthLessThan(ROOM_ID_1, PERIOD))
                .thenReturn(true);

        final var ex =
                assertThrows(
                        AppException.class,
                        () -> invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD));
        assertEquals(ErrorCode.MISSING_PREVIOUS_UTILITY_READING, ex.getErrorCode());
    }

    @Test
    void createInvoice_WithUtilityReadingForWater_ShouldCalculateWaterCost() {
        // Given
        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(null);
        currentReading.setWaterIndex(60);

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12");
        previousReading.setWaterIndex(50);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(200000); // (60-50) * 20000
        savedInvoice.setTotalAmount(3350000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(200000, result.get(0).getWaterAmount());
    }

    @Test
    void createInvoice_WithNegativeElecUsage_ShouldReturnZero() {
        // Given (currentValue < previousValue - invalid scenario)
        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setElectricIndex(50); // Lower than previous

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12");
        previousReading.setElectricIndex(100);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.empty());
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(0); // Guard clause kicks in
        savedInvoice.setWaterAmount(0);
        savedInvoice.setTotalAmount(ROOM_PRICE);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getElecAmount());
    }

    @Test
    void createInvoice_WithNegativeWaterUsage_ShouldReturnZero() {
        // Given
        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(PERIOD);
        currentReading.setWaterIndex(30); // Lower than previous

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12");
        previousReading.setWaterIndex(50);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, PERIOD))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.empty());
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(0);
        savedInvoice.setWaterAmount(0); // Guard clause kicks in
        savedInvoice.setTotalAmount(ROOM_PRICE);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getWaterAmount());
    }

    @Test
    void createInvoice_WithJanuaryPeriod_ShouldGetDecemberPreviousMonth() {
        // Given - Testing getPreviousMonth for January -> December transition
        final String januaryPeriod = "2025-01";

        final UtilityReading currentReading = new UtilityReading();
        currentReading.setRoom(room1);
        currentReading.setMonth(januaryPeriod);
        currentReading.setElectricIndex(150);
        currentReading.setWaterIndex(60);

        final UtilityReading previousReading = new UtilityReading();
        previousReading.setRoom(room1);
        previousReading.setMonth("2024-12"); // December of previous year
        previousReading.setElectricIndex(100);
        previousReading.setWaterIndex(50);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, januaryPeriod)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, januaryPeriod))
                .thenReturn(Optional.of(currentReading));
        when(utilityReadingRepository.findByRoomIdAndMonth(ROOM_ID_1, "2024-12"))
                .thenReturn(Optional.of(previousReading));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(januaryPeriod);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(200000);
        savedInvoice.setTotalAmount(3350000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, januaryPeriod);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(utilityReadingRepository, org.mockito.Mockito.atLeast(1))
                .findByRoomIdAndMonth(ROOM_ID_1, "2024-12");
    }

    @Test
    void createInvoice_WithNullElecUnitPrice_ShouldReturnZeroElecAmount() {
        // Given
        building.setElecUnitPrice(null);

        // UtilityReading path is skipped when elecUnitPrice is null,
        // so the code falls back to MeterRecord
        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        // When elecUnitPrice is null, calculateElectricityCost returns 0 immediately
        // and calculateWaterCost is still called, so we need to mock water meter record
        lenient()
                .when(
                        meterRecordRepository.findByRoomIdAndPeriodAndType(
                                ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.empty());
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);
        // UtilityReadingRepository is not called when elecUnitPrice is null, but lenient mock in
        // setUp handles it

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(0); // No elecUnitPrice
        savedInvoice.setWaterAmount(0);
        savedInvoice.setTotalAmount(ROOM_PRICE);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getElecAmount());
    }

    @Test
    void createInvoice_WithNullWaterUnitPrice_ShouldReturnZeroWaterAmount() {
        // Given
        building.setWaterUnitPrice(null);

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(elecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(waterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(150000);
        savedInvoice.setWaterAmount(0); // No waterUnitPrice
        savedInvoice.setTotalAmount(3150000);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getWaterAmount());
    }

    @Test
    void createInvoice_WithNegativeMeterRecordUsage_ShouldReturnZero() {
        // Given - MeterRecord with currentValue < previousValue
        final MeterRecord negativeElecRecord = new MeterRecord();
        negativeElecRecord.setRoom(room1);
        negativeElecRecord.setType(MeterType.ELEC);
        negativeElecRecord.setPeriod(PERIOD);
        negativeElecRecord.setPreviousValue(200);
        negativeElecRecord.setCurrentValue(100); // Less than previous

        final MeterRecord negativeWaterRecord = new MeterRecord();
        negativeWaterRecord.setRoom(room1);
        negativeWaterRecord.setType(MeterType.WATER);
        negativeWaterRecord.setPeriod(PERIOD);
        negativeWaterRecord.setPreviousValue(100);
        negativeWaterRecord.setCurrentValue(50); // Less than previous

        when(roomRepository.findByBuildingId(BUILDING_ID)).thenReturn(Arrays.asList(room1));
        when(invoiceRepository.existsByRoomIdAndPeriod(ROOM_ID_1, PERIOD)).thenReturn(false);
        when(tenantRepository.findByRoomIdAndIsContractHolderTrue(ROOM_ID_1))
                .thenReturn(Optional.of(tenant1));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.ELEC))
                .thenReturn(Optional.of(negativeElecRecord));
        when(meterRecordRepository.findByRoomIdAndPeriodAndType(ROOM_ID_1, PERIOD, MeterType.WATER))
                .thenReturn(Optional.of(negativeWaterRecord));
        when(tenantRepository.countByRoomId(ROOM_ID_1)).thenReturn(1);

        final Invoice savedInvoice = new Invoice();
        savedInvoice.setId(1);
        savedInvoice.setRoom(room1);
        savedInvoice.setTenant(tenant1);
        savedInvoice.setPeriod(PERIOD);
        savedInvoice.setRoomPrice(ROOM_PRICE);
        savedInvoice.setElecAmount(0); // Guard clause
        savedInvoice.setWaterAmount(0); // Guard clause
        savedInvoice.setTotalAmount(ROOM_PRICE);
        savedInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        final List<InvoiceResponse> result =
                invoiceService.createInvoicesForBuilding(BUILDING_ID, PERIOD);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getElecAmount());
        assertEquals(0, result.get(0).getWaterAmount());
    }

    @Test
    void getInvoices_WithEmptyResult_ShouldReturnEmptyPageResponse() {
        // Given
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Invoice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(invoiceRepository.findByBuildingIdWithFilters(BUILDING_ID, PERIOD, null, pageable))
                .thenReturn(emptyPage);

        // When
        final var result = invoiceService.getInvoices(BUILDING_ID, PERIOD, null, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getPage().getTotalElements());
    }
}
