package com.careertrack.repository;

import com.careertrack.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByInternshipId(Long internshipId);
    List<Application> findByUserId(Long userId);
    boolean existsByInternshipIdAndUserId(Long internshipId, Long userId);
}
