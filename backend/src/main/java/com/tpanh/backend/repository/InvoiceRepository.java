package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.enums.InvoiceStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    boolean existsByRoomIdAndPeriod(Integer roomId, String period);

    Page<Invoice> findByRoomBuildingIdAndPeriod(
            Integer buildingId, String period, Pageable pageable);

    Page<Invoice> findByRoomBuildingIdAndStatus(
            Integer buildingId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByRoomBuildingIdAndPeriodAndStatus(
            Integer buildingId, String period, InvoiceStatus status, Pageable pageable);

    @Query(
            "SELECT i FROM Invoice i WHERE i.room.building.id = :buildingId "
                    + "AND (:period IS NULL OR i.period = :period) "
                    + "AND (:status IS NULL OR i.status = :status)")
    Page<Invoice> findByBuildingIdWithFilters(
            @Param("buildingId") Integer buildingId,
            @Param("period") String period,
            @Param("status") InvoiceStatus status,
            Pageable pageable);

    List<Invoice> findByRoomBuildingIdAndPeriod(Integer buildingId, String period);

    boolean existsByIdAndRoomBuildingManagerId(Integer id, String managerId);

    @Query(
            "SELECT i FROM Invoice i WHERE i.status IN ('DRAFT', 'UNPAID') "
                    + "AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") java.time.LocalDate today);
}
