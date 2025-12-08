package com.tpanh.backend.repository;

import com.tpanh.backend.entity.MeterRecord;
import com.tpanh.backend.enums.MeterType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRecordRepository extends JpaRepository<MeterRecord, Integer> {

    Optional<MeterRecord> findByRoomIdAndPeriodAndType(
            Integer roomId, String period, MeterType type);
}
