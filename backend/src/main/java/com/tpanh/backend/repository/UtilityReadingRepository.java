package com.tpanh.backend.repository;

import com.tpanh.backend.entity.UtilityReading;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilityReadingRepository extends JpaRepository<UtilityReading, Integer> {

    Optional<UtilityReading> findByRoomIdAndMonth(Integer roomId, String month);

    List<UtilityReading> findByRoomIdOrderByMonthDesc(Integer roomId);

    List<UtilityReading> findByRoomBuildingIdAndMonth(Integer buildingId, String month);

    boolean existsByRoomIdAndMonthLessThan(Integer roomId, String month);

    boolean existsByIdAndRoomBuildingManagerId(Integer id, String managerId);

    long countByRoomId(Integer roomId);
}
