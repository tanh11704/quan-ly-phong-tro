package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {

    Optional<Tenant> findByRoomIdAndIsContractHolderTrue(Integer roomId);

    Integer countByRoomId(Integer roomId);

    List<Tenant> findByRoomIdOrderByStartDateDesc(Integer roomId);

    Page<Tenant> findByRoomIdOrderByStartDateDesc(Integer roomId, Pageable pageable);
}
