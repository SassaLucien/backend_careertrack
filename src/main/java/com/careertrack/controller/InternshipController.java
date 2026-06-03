package com.careertrack.controller;

import com.careertrack.model.*;
import com.careertrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internships")
@CrossOrigin(origins = "*")
public class InternshipController {

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping("/post")
    public ResponseEntity<?> postInternship(@RequestBody InternshipRequest request) {
        try {
            Company company;
            if (request.getCompanyId() != null) {
                company = companyRepository.findById(request.getCompanyId()).orElse(null);
                if (company == null) {
                    company = companyRepository.findByUserId(request.getCompanyId()).orElse(null);
                }
                if (company == null) {
                    return ResponseEntity.badRequest().body(java.util.Map.of("error", "Company not found"));
                }
            } else {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "companyId required"));
            }

            Internship internship = new Internship();
            internship.setCompany(company);
            internship.setTitle(request.getTitle());
            internship.setDescription(request.getDescription());
            internship.setRequirements(request.getRequirements());
            internship.setLocation(request.getLocation());
            internship.setDuration(request.getDuration());
            internship.setImageUrl(request.getImageUrl());
            if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
                internship.setStartDate(LocalDate.parse(request.getStartDate()));
            }
            if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
                internship.setEndDate(LocalDate.parse(request.getEndDate()));
            }
            if (request.getExpirationDate() != null && !request.getExpirationDate().isEmpty()) {
                internship.setExpirationDate(LocalDate.parse(request.getExpirationDate()));
            }
            internshipRepository.save(internship);

            return ResponseEntity.ok(java.util.Map.of("success", true, "internship", java.util.Map.of(
                "id", internship.getId(),
                "title", internship.getTitle(),
                "description", internship.getDescription()
            )));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInternship(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé"));

            Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));

            if (!internship.getCompany().getId().equals(company.getId())) {
                throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce stage");
            }

            internshipRepository.delete(internship);
            return ResponseEntity.ok(java.util.Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/company/{userId}")
    public ResponseEntity<?> getCompanyInternships(@PathVariable Long userId) {
        try {
            Company company = companyRepository.findById(userId).orElse(null);
            if (company == null) {
                company = companyRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée pour cet utilisateur"));
            }

            List<Internship> internships = internshipRepository.findByCompanyId(company.getId());
            List<InternshipResponse> responses = internships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllInternships() {
        try {
            List<Internship> internships = internshipRepository.findAllWithCompany();
            List<InternshipResponse> responses = internships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private InternshipResponse toResponse(Internship internship) {
        InternshipResponse response = new InternshipResponse();
        response.setId(internship.getId());
        response.setTitle(internship.getTitle());
        response.setDescription(internship.getDescription());
        response.setRequirements(internship.getRequirements());
        response.setLocation(internship.getLocation());
        response.setImageUrl(internship.getImageUrl());
        response.setExpirationDate(internship.getExpirationDate());
        response.setStartDate(internship.getStartDate());
        response.setEndDate(internship.getEndDate());
        response.setPostedAt(internship.getPostedAt());
        response.setCompanyName(internship.getCompany().getCompanyName());
        response.setCompanyProfilePhoto(internship.getCompany().getProfilePhoto());
        response.setCompanySector(internship.getCompany().getSector());
        response.setCompanyId(internship.getCompany().getId());
        response.setStatus(internship.getStatus());
        return response;
    }

    static class InternshipRequest {
        private Long companyId;
        private String title;
        private String description;
        private String[] requirements;
        private String location;
        private String duration;
        private String imageUrl;
        private String startDate;
        private String endDate;
        private String expirationDate;

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String[] getRequirements() { return requirements; }
        public void setRequirements(String[] requirements) { this.requirements = requirements; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getExpirationDate() { return expirationDate; }
        public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    }

    static class InternshipResponse {
        private Long id;
        private String title;
        private String description;
        private String[] requirements;
        private String location;
        private String imageUrl;
        private LocalDate expirationDate;
        private LocalDate startDate;
        private LocalDate endDate;
        private java.time.LocalDateTime postedAt;
        private String companyName;
        private String companyProfilePhoto;
        private String companySector;
        private Long companyId;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String[] getRequirements() { return requirements; }
        public void setRequirements(String[] requirements) { this.requirements = requirements; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public java.time.LocalDateTime getPostedAt() { return postedAt; }
        public void setPostedAt(java.time.LocalDateTime postedAt) { this.postedAt = postedAt; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getCompanyProfilePhoto() { return companyProfilePhoto; }
        public void setCompanyProfilePhoto(String companyProfilePhoto) { this.companyProfilePhoto = companyProfilePhoto; }
        public String getCompanySector() { return companySector; }
        public void setCompanySector(String companySector) { this.companySector = companySector; }
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    static class SuccessResponse {
        private boolean success;
        private Long internshipId;

        public SuccessResponse(boolean success, Long internshipId) {
            this.success = success;
            this.internshipId = internshipId;
        }

        public boolean isSuccess() { return success; }
        public Long getInternshipId() { return internshipId; }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}