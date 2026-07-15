package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.FeedbackTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackTicketRepository extends JpaRepository<FeedbackTicket, Long> {

    List<FeedbackTicket> findAllByOrderByCreatedDateDesc();

    List<FeedbackTicket> findBySubmittedByIdOrderByCreatedDateDesc(Long submittedById);

    void deleteBySubmittedById(Long submittedById);
}
