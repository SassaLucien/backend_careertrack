package com.careertrack.repository;

import com.careertrack.model.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InternshipRepository extends JpaRepository<Internship, Long> {
List<Internship> findByCompanyId(Long companyId);

@Query("SELECT i FROM Internship i JOIN FETCH i.company c JOIN FETCH c.user u ORDER BY i.postedAt DESC")
List<Internship> findAllWithCompany();

@Query("SELECT i FROM Internship i JOIN FETCH i.company c JOIN FETCH c.user u WHERE i.id = :id")
Internship findByIdWithCompany(@Param("id") Long id);
}