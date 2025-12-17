package com.tpanh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpanh.backend.entity.UtilityReading;

public interface UtilityReadingRepository extends JpaRepository<UtilityReading, Integer> {

    Optional<UtilityReading> findByRoomIdAndMonth(Integer roomId, String month);

    List<UtilityReading> findByRoomIdOrderByMonthDesc(Integer roomId);

    List<UtilityReading> findByRoomBuildingIdAndMonth(Integer buildingId, String month);
}
