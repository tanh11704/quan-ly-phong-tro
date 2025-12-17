package com.tpanh.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tpanh.backend.entity.Room;
import com.tpanh.backend.enums.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    List<Room> findByBuildingId(Integer buildingId);

    Page<Room> findByBuildingId(Integer buildingId, Pageable pageable);

    Page<Room> findByBuildingIdAndStatus(Integer buildingId, RoomStatus status, Pageable pageable);

    List<Room> findByBuildingIdAndStatus(Integer buildingId, RoomStatus status);
}
