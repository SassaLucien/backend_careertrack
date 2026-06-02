package com.careertrack.controller;

import com.careertrack.model.*;
import com.careertrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public ResponseEntity<?> applyToInternship(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long internshipId = Long.valueOf(request.get("internshipId").toString());
            String message = request.get("message") != null ? request.get("message").toString() : "";

            logger.info("=== APPLY TO INTERNSHIP REQUEST ===");
            logger.info("Authenticated user ID: {}", userId);
            logger.info("Internship ID: {}", internshipId);
            logger.info("Message: {}", message);

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

            logger.info("User found - ID: {}, Email: {}, Role: {}", user.getId(), user.getEmail(), user.getRole());

            if ("COMPANY".equals(user.getRole())) {
                logger.warn("Authorization result: BLOCKED - Company user {} cannot apply to internship {}", userId, internshipId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Company accounts are not allowed to apply for internships."));
            }

            logger.info("Authorization result: ALLOWED - Role: {}", user.getRole());

            boolean exists = applicationRepository.existsByInternshipIdAndUserId(internshipId, userId);
            logger.info("Checking existing application for internshipId: {} and userId: {} -> exists: {}", internshipId, userId, exists);
            if (exists) {
                logger.warn("User {} already applied to internship {}", userId, internshipId);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "You have already applied to this internship."));
            }

            Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));
            logger.info("Internship found - ID: {}, Title: {}", internship.getId(), internship.getTitle());

            Application application = new Application();
            application.setUser(user);
            application.setInternship(internship);
            application.setMessage(message);
            application.setStatus("PENDING");
            Application savedApplication = applicationRepository.save(application);
            logger.info("Database insert result: application ID={}, status={}, appliedAt={}", savedApplication.getId(), savedApplication.getStatus(), savedApplication.getAppliedAt());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Application submitted successfully",
                "applicationId", savedApplication.getId()
            ));

        } catch (Exception e) {
            logger.error("Apply error for userId={}, internshipId={}", request.get("userId"), request.get("internshipId"), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "An error occurred while processing your application"));
        }
    }

    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<?> getApplicationsByInternship(@PathVariable Long internshipId) {
        try {
            List<Application> applications = applicationRepository.findByInternshipId(internshipId);
            List<Map<String, Object>> result = applications.stream()
                .map(app -> toApplicantMap(app))
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching applications for internship {}", internshipId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getApplicationsByCompany(@PathVariable Long companyId) {
        try {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company == null) {
                company = companyRepository.findByUserId(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found for userId: " + companyId));
            }
            List<Internship> internships = internshipRepository.findByCompanyId(company.getId());
            Set<Long> internshipIds = internships.stream()
                .map(Internship::getId)
                .collect(Collectors.toSet());

            List<Application> allApplications = new ArrayList<>();
            for (Long iid : internshipIds) {
                allApplications.addAll(applicationRepository.findByInternshipId(iid));
            }

            List<Map<String, Object>> result = allApplications.stream()
                .map(app -> {
                    Map<String, Object> map = toApplicantMap(app);
                    Internship intship = app.getInternship();
                    if (intship != null) {
                        map.put("internship_title", intship.getTitle());
                        map.put("internship_location", intship.getLocation());
                        map.put("internship_id", intship.getId());
                    }
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching applications for company {}", companyId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/accepted/internship/{internshipId}")
    public ResponseEntity<?> getAcceptedByInternship(@PathVariable Long internshipId) {
        try {
            List<Application> applications = applicationRepository.findByInternshipId(internshipId);
            List<Map<String, Object>> result = applications.stream()
                .filter(app -> "ACCEPTED".equals(app.getStatus()))
                .map(app -> {
                    Map<String, Object> map = toApplicantMap(app);
                    Internship internship = app.getInternship();
                    if (internship != null && internship.getCompany() != null) {
                        Company company = internship.getCompany();
                        map.put("companyId", company.getId());
                        map.put("companyName", company.getCompanyName());
                        map.put("companyProfilePhoto", company.getProfilePhoto());
                        map.put("companySector", company.getSector());
                    }
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching accepted applications for internship {}", internshipId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/company/{companyId}/statistics")
    public ResponseEntity<?> getCompanyStatistics(@PathVariable Long companyId) {
        try {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company == null) {
                company = companyRepository.findByUserId(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found for userId: " + companyId));
            }
            List<Internship> internships = internshipRepository.findByCompanyId(company.getId());
            Set<Long> internshipIds = internships.stream().map(Internship::getId).collect(Collectors.toSet());

            List<Application> allApplications = new ArrayList<>();
            Map<Long, List<Application>> applicationsByInternship = new HashMap<>();
            for (Long iid : internshipIds) {
                List<Application> apps = applicationRepository.findByInternshipId(iid);
                applicationsByInternship.put(iid, apps);
                allApplications.addAll(apps);
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalInternships", internships.size());

            long totalActive = internships.stream().filter(i -> "ACTIVE".equals(i.getStatus())).count();
            long totalExpired = internships.stream().filter(i -> "EXPIRED".equals(i.getStatus())).count();
            long totalClosed = internships.stream().filter(i -> "CLOSED".equals(i.getStatus())).count();
            stats.put("totalActiveInternships", totalActive);
            stats.put("totalExpiredInternships", totalExpired);
            stats.put("totalClosedInternships", totalClosed);

            stats.put("totalApplications", allApplications.size());
            long totalAccepted = allApplications.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
            long totalRejected = allApplications.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
            long totalPending = allApplications.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
            stats.put("totalAccepted", totalAccepted);
            stats.put("totalRejected", totalRejected);
            stats.put("totalPending", totalPending);
            stats.put("acceptanceRate", allApplications.isEmpty() ? 0 : Math.round((totalAccepted * 100.0 / allApplications.size())));
            stats.put("rejectionRate", allApplications.isEmpty() ? 0 : Math.round((totalRejected * 100.0 / allApplications.size())));

            List<Map<String, Object>> internshipPerformance = new ArrayList<>();
            for (Internship internship : internships) {
                List<Application> apps = applicationsByInternship.getOrDefault(internship.getId(), Collections.emptyList());
                long accepted = apps.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
                long rejected = apps.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
                long pending = apps.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
                Map<String, Object> perf = new LinkedHashMap<>();
                perf.put("id", internship.getId());
                perf.put("title", internship.getTitle());
                perf.put("status", internship.getStatus());
                perf.put("views", 0);
                perf.put("applications", apps.size());
                perf.put("accepted", accepted);
                perf.put("rejected", rejected);
                perf.put("pending", pending);
                perf.put("acceptanceRate", apps.isEmpty() ? 0 : Math.round((accepted * 100.0 / apps.size())));
                perf.put("location", internship.getLocation());
                perf.put("postedAt", internship.getPostedAt() != null ? internship.getPostedAt().format(DATE_FORMATTER) : null);
                internshipPerformance.add(perf);
            }
            stats.put("internships", internshipPerformance);

            long studentApps = 0;
            long professorApps = 0;
            for (Application app : allApplications) {
                User user = app.getUser();
                if (user != null) {
                    String role = user.getRole();
                    if ("STUDENT".equals(role)) studentApps++;
                    else if ("TEACHER".equals(role)) professorApps++;
                }
            }
            Map<String, Object> candidateBreakdown = new LinkedHashMap<>();
            candidateBreakdown.put("studentApplications", studentApps);
            candidateBreakdown.put("professorApplications", professorApps);
            candidateBreakdown.put("studentPercentage", allApplications.isEmpty() ? 0 : Math.round((studentApps * 100.0 / allApplications.size())));
            candidateBreakdown.put("professorPercentage", allApplications.isEmpty() ? 0 : Math.round((professorApps * 100.0 / allApplications.size())));
            stats.put("candidateBreakdown", candidateBreakdown);

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate thirtyDaysAgo = today.minusDays(30);
            Map<String, Object> dailyAnalytics = new LinkedHashMap<>();
            for (int i = 29; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                String dateStr = date.toString();
                long count = allApplications.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().toLocalDate().equals(date))
                    .count();
                dailyAnalytics.put(dateStr, count);
            }
            stats.put("dailyAnalytics", dailyAnalytics);

            String[] daysOfWeek = new String[]{"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"};
            Map<String, Map<String, Long>> weeklyAnalytics = new LinkedHashMap<>();
            for (String day : daysOfWeek) {
                java.time.DayOfWeek dow = java.time.DayOfWeek.valueOf(day);
                long received = allApplications.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().getDayOfWeek() == dow)
                    .count();
                long accepted = allApplications.stream()
                    .filter(a -> "ACCEPTED".equals(a.getStatus()) && a.getUpdatedAt() != null && a.getUpdatedAt().getDayOfWeek() == dow)
                    .count();
                long rejected = allApplications.stream()
                    .filter(a -> "REJECTED".equals(a.getStatus()) && a.getUpdatedAt() != null && a.getUpdatedAt().getDayOfWeek() == dow)
                    .count();
                Map<String, Long> dayMap = new LinkedHashMap<>();
                dayMap.put("received", received);
                dayMap.put("accepted", accepted);
                dayMap.put("rejected", rejected);
                weeklyAnalytics.put(day, dayMap);
            }
            stats.put("weeklyAnalytics", weeklyAnalytics);

            Map<String, Long> hourlyAnalytics = new LinkedHashMap<>();
            for (int h = 0; h < 24; h++) {
                final int hour = h;
                long received = allApplications.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().getHour() == hour)
                    .count();
                hourlyAnalytics.put(String.valueOf(hour), received);
            }
            stats.put("hourlyAnalytics", hourlyAnalytics);

            Map<String, Map<String, Long>> monthlyAnalytics = new LinkedHashMap<>();
            for (int m = 1; m <= 12; m++) {
                final int month = m;
                String monthName = java.time.Month.of(m).name();
                long received = allApplications.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().getMonthValue() == month)
                    .count();
                long accepted = allApplications.stream()
                    .filter(a -> "ACCEPTED".equals(a.getStatus()) && a.getUpdatedAt() != null && a.getUpdatedAt().getMonthValue() == month)
                    .count();
                long rejected = allApplications.stream()
                    .filter(a -> "REJECTED".equals(a.getStatus()) && a.getUpdatedAt() != null && a.getUpdatedAt().getMonthValue() == month)
                    .count();
                Map<String, Long> monthMap = new LinkedHashMap<>();
                monthMap.put("received", received);
                monthMap.put("accepted", accepted);
                monthMap.put("rejected", rejected);
                monthlyAnalytics.put(monthName, monthMap);
            }
            stats.put("monthlyAnalytics", monthlyAnalytics);

            List<Map<String, Object>> ranking = internshipPerformance.stream()
                .sorted((a, b) -> Long.compare((Long)b.get("applications"), (Long)a.get("applications")))
                .collect(Collectors.toList());
            stats.put("ranking", ranking);

            List<Map<String, Object>> timeline = new ArrayList<>();
            for (Internship internship : internships) {
                timeline.add(Map.of(
                    "type", "INTERNSHIP_POSTED",
                    "title", "Nouveau stage publié",
                    "description", internship.getTitle(),
                    "date", internship.getPostedAt() != null ? internship.getPostedAt().format(DATE_FORMATTER) : null,
                    "internshipId", internship.getId()
                ));
            }
            for (Application app : allApplications) {
                String type = "APPLICATION_RECEIVED";
                String title = "Nouvelle candidature reçue";
                if ("ACCEPTED".equals(app.getStatus())) { type = "APPLICATION_ACCEPTED"; title = "Candidature acceptée"; }
                else if ("REJECTED".equals(app.getStatus())) { type = "APPLICATION_REJECTED"; title = "Candidature rejetée"; }
                User appUser = app.getUser();
                String applicantName = appUser != null ? (appUser.getRole() + " user") : "Candidat";
                timeline.add(Map.of(
                    "type", type,
                    "title", title,
                    "description", applicantName + " pour \"" + (app.getInternship() != null ? app.getInternship().getTitle() : "Stage") + "\"",
                    "date", app.getAppliedAt() != null ? app.getAppliedAt().format(DATE_FORMATTER) : null,
                    "internshipId", app.getInternship() != null ? app.getInternship().getId() : null
                ));
            }
            timeline.sort((a, b) -> {
                String da = (String)a.get("date");
                String db = (String)b.get("date");
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });
            List<Map<String, Object>> limitedTimeline = timeline.stream().limit(50).collect(Collectors.toList());
            stats.put("timeline", limitedTimeline);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching statistics for company {}", companyId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/counts/{internshipId}")
    public ResponseEntity<?> getApplicationCounts(@PathVariable Long internshipId) {
        try {
            List<Application> applications = applicationRepository.findByInternshipId(internshipId);
            long total = applications.size();
            long pending = applications.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
            long accepted = applications.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
            long rejected = applications.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
            return ResponseEntity.ok(Map.of(
                "total", total,
                "pending", pending,
                "accepted", accepted,
                "rejected", rejected
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        try {
            applicationRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Application deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting application {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
            application.setStatus(status);
            application.setUpdatedAt(LocalDateTime.now());
            applicationRepository.save(application);
            return ResponseEntity.ok(Map.of("success", true, "applicationId", id, "status", status));
        } catch (Exception e) {
            logger.error("Error updating application status for id={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toApplicantMap(Application app) {
        Map<String, Object> map = new LinkedHashMap<>();
        User user = app.getUser();
        String role = user != null ? user.getRole() : "UNKNOWN";

        map.put("id", app.getId());
        map.put("applicationId", app.getId());
        map.put("userId", user != null ? user.getId() : null);
        map.put("role", role);
        map.put("email", user != null ? user.getEmail() : null);
        map.put("phone", user != null ? user.getPhone() : null);
        map.put("status", app.getStatus());
        map.put("message", app.getMessage());
        map.put("appliedAt", app.getAppliedAt() != null ? app.getAppliedAt().format(DATE_FORMATTER) : null);
        map.put("applied_at", app.getAppliedAt() != null ? app.getAppliedAt().format(DATE_FORMATTER) : null);

        if ("STUDENT".equals(role)) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                map.put("firstName", student.getFirstName());
                map.put("lastName", student.getLastName());
                map.put("first_name", student.getFirstName());
                map.put("last_name", student.getLastName());
                map.put("student_first_name", student.getFirstName());
                map.put("student_last_name", student.getLastName());
                map.put("profilePhoto", student.getProfilePhoto());
                map.put("profile_photo", student.getProfilePhoto());
                map.put("student_profile_photo", student.getProfilePhoto());
                map.put("university", student.getUniversity());
                map.put("graduationYear", student.getGraduationYear());
                map.put("graduation_year", student.getGraduationYear());
                map.put("skills", student.getSkills() != null ? Arrays.toString(student.getSkills()).replaceAll("[\\[\\]]", "").split(", ") : new String[0]);
                map.put("bio", student.getUniversity());
                map.put("degree", student.getDegree());
                map.put("diplomaImage", student.getDiplomaImage());
            } else {
                map.put("firstName", null);
                map.put("lastName", null);
                map.put("profilePhoto", null);
            }
        } else if ("TEACHER".equals(role)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
            if (teacher != null) {
                map.put("firstName", teacher.getFirstName());
                map.put("lastName", teacher.getLastName());
                map.put("first_name", teacher.getFirstName());
                map.put("last_name", teacher.getLastName());
                map.put("teacher_first_name", teacher.getFirstName());
                map.put("teacher_last_name", teacher.getLastName());
                map.put("profilePhoto", teacher.getProfilePhoto());
                map.put("profile_photo", teacher.getProfilePhoto());
                map.put("teacher_profile_photo", teacher.getProfilePhoto());
                map.put("department", teacher.getDepartment());
                map.put("subject", teacher.getSubject());
                map.put("experience", teacher.getExperience());
                map.put("bio", teacher.getDepartment());
                map.put("filiere", teacher.getFiliere());
            } else {
                map.put("firstName", null);
                map.put("lastName", null);
                map.put("profilePhoto", null);
            }
        }

        return map;
    }

    @GetMapping("/count/{internshipId}")
    public ResponseEntity<?> getApplicationCount(@PathVariable Long internshipId) {
        try {
            long count = applicationRepository.findByInternshipId(internshipId).size();
            return ResponseEntity.ok(Map.of("total", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
