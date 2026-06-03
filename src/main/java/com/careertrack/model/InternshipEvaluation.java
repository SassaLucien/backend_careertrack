package com.careertrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "internship_evaluations")
public class InternshipEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internship_id", nullable = false)
    private Long internshipId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "behavior_rating")
    private String behaviorRating;

    @Column(name = "skills_rating")
    private String skillsRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "evaluation_date")
    private LocalDate evaluationDate = LocalDate.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInternshipId() { return internshipId; }
    public void setInternshipId(Long internshipId) { this.internshipId = internshipId; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getBehaviorRating() { return behaviorRating; }
    public void setBehaviorRating(String behaviorRating) { this.behaviorRating = behaviorRating; }
    public String getSkillsRating() { return skillsRating; }
    public void setSkillsRating(String skillsRating) { this.skillsRating = skillsRating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
