package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.SalesPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesPostRepository extends JpaRepository<SalesPost, Long> {

    List<SalesPost> findAllByOrderByCreatedDateDesc();
}
