package com.careertrack.repository;

import com.careertrack.model.InternshipEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternshipEvaluationRepository extends JpaRepository<InternshipEvaluation, Long> {
    List<InternshipEvaluation> findByApplicantId(Long applicantId);
    List<InternshipEvaluation> findByInternshipId(Long internshipId);
    List<InternshipEvaluation> findByCompanyId(Long companyId);
}
