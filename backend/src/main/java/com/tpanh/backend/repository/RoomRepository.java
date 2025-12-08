package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    List<Room> findByBuildingId(Integer buildingId);
}
