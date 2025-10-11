package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.ScheduledEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IScheduledEmailRepository extends JpaRepository<ScheduledEmail, Long> {
    List<ScheduledEmail> findByStatusAndSendTimeBefore(String status, LocalDateTime now);
}
