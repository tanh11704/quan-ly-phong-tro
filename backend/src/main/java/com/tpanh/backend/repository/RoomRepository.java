package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Room;
import com.tpanh.backend.enums.RoomStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    boolean existsByIdAndBuildingManagerId(Integer id, String managerId);

    Optional<Room> findByIdAndBuildingManagerId(Integer id, String managerId);

    List<Room> findByBuildingIdAndBuildingManagerId(Integer buildingId, String managerId);

    Page<Room> findByBuildingIdAndBuildingManagerId(
            Integer buildingId, String managerId, Pageable pageable);

    Page<Room> findByBuildingIdAndStatusAndBuildingManagerId(
            Integer buildingId, RoomStatus status, String managerId, Pageable pageable);

    List<Room> findByBuildingId(Integer buildingId);

    Page<Room> findByBuildingId(Integer buildingId, Pageable pageable);

    Page<Room> findByBuildingIdAndStatus(Integer buildingId, RoomStatus status, Pageable pageable);

    List<Room> findByBuildingIdAndStatus(Integer buildingId, RoomStatus status);
}
