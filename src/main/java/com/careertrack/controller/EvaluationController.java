package com.careertrack.controller;

import com.careertrack.model.InternshipEvaluation;
import com.careertrack.repository.ApplicationRepository;
import com.careertrack.repository.InternshipEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EvaluationController.class);

    @Autowired
    private InternshipEvaluationRepository evaluationRepository;

    @Autowired
    private ApplicationRepository applicationRepository;
    public ResponseEntity<?> createEvaluation(@RequestBody Map<String, Object> request) {
        try {
            Long internshipId = Long.valueOf(request.get("internshipId").toString());
            Long applicantId = Long.valueOf(request.get("applicantId").toString());
            Long companyId = Long.valueOf(request.get("companyId").toString());
            String behaviorRating = request.get("behaviorRating") != null ? request.get("behaviorRating").toString() : null;
            String skillsRating = request.get("skillsRating") != null ? request.get("skillsRating").toString() : null;
            String comment = request.get("comment") != null ? request.get("comment").toString() : "";

            logger.info("=== CREATE EVALUATION REQUEST ===");
            logger.info("Internship ID: {}, Applicant ID: {}, Company ID: {}", internshipId, applicantId, companyId);

            InternshipEvaluation evaluation = new InternshipEvaluation();
            evaluation.setInternshipId(internshipId);
            evaluation.setApplicantId(applicantId);
            evaluation.setCompanyId(companyId);
            evaluation.setBehaviorRating(behaviorRating);
            evaluation.setSkillsRating(skillsRating);
            evaluation.setComment(comment);

            InternshipEvaluation saved = evaluationRepository.save(evaluation);
            logger.info("Evaluation saved - ID: {}, Date: {}", saved.getId(), saved.getEvaluationDate());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Evaluation submitted successfully",
                "evaluationId", saved.getId()
            ));
        } catch (Exception e) {
            logger.error("Error creating evaluation", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<?> getEvaluationsByApplicant(@PathVariable Long applicantId) {
        try {
            List<InternshipEvaluation> evaluations = evaluationRepository.findByApplicantId(applicantId);
            List<Map<String, Object>> result = evaluations.stream()
                .map(ev -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", ev.getId());
                    map.put("internshipId", ev.getInternshipId());
                    map.put("applicantId", ev.getApplicantId());
                    map.put("companyId", ev.getCompanyId());
                    map.put("behaviorRating", ev.getBehaviorRating());
                    map.put("skillsRating", ev.getSkillsRating());
                    map.put("comment", ev.getComment());
                    map.put("evaluationDate", ev.getEvaluationDate() != null ? ev.getEvaluationDate().toString() : null);
                    map.put("createdAt", ev.getCreatedAt() != null ? ev.getCreatedAt().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching evaluations for applicant {}", applicantId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEvaluationsByUser(@PathVariable Long userId) {
        try {
            List<InternshipEvaluation> evaluations = evaluationRepository.findAll();
            List<Map<String, Object>> result = evaluations.stream()
                .filter(ev -> {
                    if (ev.getApplicantId() == null) return false;
                    var appOpt = applicationRepository.findById(ev.getApplicantId());
                    return appOpt.isPresent() && appOpt.get().getUser().getId().equals(userId);
                })
                .map(ev -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", ev.getId());
                    map.put("internshipId", ev.getInternshipId());
                    map.put("applicantId", ev.getApplicantId());
                    map.put("companyId", ev.getCompanyId());
                    map.put("behaviorRating", ev.getBehaviorRating());
                    map.put("skillsRating", ev.getSkillsRating());
                    map.put("comment", ev.getComment());
                    map.put("evaluationDate", ev.getEvaluationDate() != null ? ev.getEvaluationDate().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching evaluations for user {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<?> getEvaluationsByInternship(@PathVariable Long internshipId) {
        try {
            List<InternshipEvaluation> evaluations = evaluationRepository.findByInternshipId(internshipId);
            List<Map<String, Object>> result = evaluations.stream()
                .map(ev -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", ev.getId());
                    map.put("internshipId", ev.getInternshipId());
                    map.put("applicantId", ev.getApplicantId());
                    map.put("companyId", ev.getCompanyId());
                    map.put("behaviorRating", ev.getBehaviorRating());
                    map.put("skillsRating", ev.getSkillsRating());
                    map.put("comment", ev.getComment());
                    map.put("evaluationDate", ev.getEvaluationDate() != null ? ev.getEvaluationDate().toString() : null);
                    map.put("createdAt", ev.getCreatedAt() != null ? ev.getCreatedAt().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching evaluations for internship {}", internshipId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
