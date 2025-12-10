package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Building;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Integer> {
    List<Building> findByManagerId(String managerId);

    Page<Building> findByManagerId(String managerId, Pageable pageable);

    Optional<Building> findByIdAndManagerId(Integer id, String managerId);
}
