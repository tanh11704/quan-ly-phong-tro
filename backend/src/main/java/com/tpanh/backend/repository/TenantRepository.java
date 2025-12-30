package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {

    Optional<Tenant> findByRoomIdAndIsContractHolderTrue(Integer roomId);

    Optional<Tenant> findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(Integer roomId);

    Integer countByRoomId(Integer roomId);

    List<Tenant> findByRoomIdOrderByStartDateDesc(Integer roomId);

    Page<Tenant> findByRoomIdOrderByStartDateDesc(Integer roomId, Pageable pageable);

    @Query(
            "SELECT t FROM Tenant t WHERE t.room.building.id = :buildingId "
                    + "AND (:roomId IS NULL OR t.room.id = :roomId) "
                    + "AND (:active IS NULL OR (:active = true AND t.endDate IS NULL) OR (:active = false AND t.endDate IS NOT NULL))")
    Page<Tenant> findByRoomBuildingId(
            @Param("buildingId") Integer buildingId,
            @Param("roomId") Integer roomId,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query(
            "SELECT t FROM Tenant t WHERE t.room.id = :roomId "
                    + "AND (:active IS NULL OR (:active = true AND t.endDate IS NULL) OR (:active = false AND t.endDate IS NOT NULL))")
    Page<Tenant> findByRoomIdWithFilter(
            @Param("roomId") Integer roomId, @Param("active") Boolean active, Pageable pageable);

    @Query(
            "SELECT t FROM Tenant t WHERE "
                    + "(:active IS NULL OR (:active = true AND t.endDate IS NULL) OR (:active = false AND t.endDate IS NOT NULL))")
    Page<Tenant> findAllWithFilter(@Param("active") Boolean active, Pageable pageable);
}
