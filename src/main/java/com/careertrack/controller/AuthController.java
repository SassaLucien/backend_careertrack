package com.careertrack.controller;

import com.careertrack.model.*;
import com.careertrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = new User(request.getEmail(), request.getPassword(), request.getPhone(), request.getRole());
            user.setProfilePhoto(request.getProfilePhoto());
            User savedUser = userRepository.save(user);
            
            String firstName = null;
            String lastName = null;
            
            if ("STUDENT".equals(request.getRole())) {
                Student student = new Student();
                student.setUser(savedUser);
                student.setFirstName(request.getProfile().getFirstName());
                student.setLastName(request.getProfile().getLastName());
                student.setUniversity(request.getProfile().getUniversity());
                student.setGraduationYear(request.getProfile().getGraduationYear());
                student.setSkills(request.getProfile().getSkills());
                student.setEmail(request.getEmail());
                student.setPasswordHash(request.getPassword());
                student.setProfilePhoto(request.getProfilePhoto());
                studentRepository.save(student);
                firstName = request.getProfile().getFirstName();
                lastName = request.getProfile().getLastName();
            } else if ("TEACHER".equals(request.getRole())) {
                Teacher teacher = new Teacher();
                teacher.setUser(savedUser);
                teacher.setFirstName(request.getTeacherProfile().getFirstName());
                teacher.setLastName(request.getTeacherProfile().getLastName());
                teacher.setDepartment(request.getTeacherProfile().getDepartment());
                teacher.setSubject(request.getTeacherProfile().getSubject());
                teacher.setExperience(request.getTeacherProfile().getExperience());
                teacher.setEmail(request.getEmail());
                teacher.setPasswordHash(request.getPassword());
                teacher.setProfilePhoto(request.getProfilePhoto());
                teacherRepository.save(teacher);
                firstName = request.getTeacherProfile().getFirstName();
                lastName = request.getTeacherProfile().getLastName();
            } else if ("COMPANY".equals(request.getRole())) {
                Company company = new Company();
                company.setUser(savedUser);
                company.setCompanyName(request.getCompanyProfile().getCompanyName());
                company.setEmail(request.getCompanyProfile().getEmail());
                company.setSector(request.getCompanyProfile().getSector());
                company.setAddress(request.getCompanyProfile().getAddress());
                company.setWebsite(request.getCompanyProfile().getWebsite());
                company.setDescription(request.getCompanyProfile().getDescription());
                company.setProfilePhoto(request.getCompanyProfile().getProfilePhoto());
                company.setPasswordHash(request.getPassword());
                companyRepository.save(company);
                firstName = request.getCompanyProfile().getCompanyName();
                lastName = "";
            }
            
            return ResponseEntity.ok(new AuthResponse(true, savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), firstName, lastName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.status(401).body(new ErrorResponse("Invalid credentials"));
            }
            
            String firstName = null;
            String lastName = null;
            Company company = null;

            if ("STUDENT".equals(user.getRole())) {
                Student student = studentRepository.findByUserId(user.getId()).orElse(null);
                if (student != null) {
                    firstName = student.getFirstName();
                    lastName = student.getLastName();
                }
            } else if ("TEACHER".equals(user.getRole())) {
                Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
                if (teacher != null) {
                    firstName = teacher.getFirstName();
                    lastName = teacher.getLastName();
                }
            } else if ("COMPANY".equals(user.getRole())) {
                company = companyRepository.findByUserId(user.getId()).orElse(null);
                if (company != null) {
                    firstName = company.getCompanyName();
                    lastName = null;
                } else {
                    firstName = user.getEmail().split("@")[0];
                }
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("firstName", firstName);
            response.put("lastName", lastName);
            if ("COMPANY".equals(user.getRole()) && company != null) {
                response.put("companyId", company.getId());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestParam(required = false) String email) {
        try {
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Missing email"));
            }
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("id", user.getId());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("phone", user.getPhone());
            profile.put("profilePhoto", user.getProfilePhoto());
            
            if ("STUDENT".equals(user.getRole())) {
                Student s = studentRepository.findByUserId(user.getId()).orElse(null);
                if (s != null) {
                    profile.put("studentId", s.getId());
                    profile.put("firstName", s.getFirstName());
                    profile.put("lastName", s.getLastName());
                    profile.put("university", s.getUniversity());
                    profile.put("graduationYear", s.getGraduationYear());
                    profile.put("skills", s.getSkills());
                    profile.put("degree", s.getDegree());
                    profile.put("diplomaImage", s.getDiplomaImage());
                }
            } else if ("TEACHER".equals(user.getRole())) {
                Teacher t = teacherRepository.findByUserId(user.getId()).orElse(null);
                if (t != null) {
                    profile.put("teacherId", t.getId());
                    profile.put("firstName", t.getFirstName());
                    profile.put("lastName", t.getLastName());
                    profile.put("department", t.getDepartment());
                    profile.put("subject", t.getSubject());
                    profile.put("experience", t.getExperience());
                    profile.put("filiere", t.getFiliere());
                }
            } else if ("COMPANY".equals(user.getRole())) {
                Company c = companyRepository.findByUserId(user.getId()).orElse(null);
                if (c != null) {
                    profile.put("companyId", c.getId());
                    profile.put("companyName", c.getCompanyName());
                    profile.put("sector", c.getSector());
                    profile.put("address", c.getAddress());
                    profile.put("website", c.getWebsite());
                    profile.put("description", c.getDescription());
                }
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    static class RegisterRequest {
        private String email;
        private String password;
        private String phone;
        private String role;
        private String profilePhoto;
        private StudentProfile profile;
        private TeacherProfile teacherProfile;
        private CompanyProfile companyProfile;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getProfilePhoto() { return profilePhoto; }
        public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
        public StudentProfile getProfile() { return profile; }
        public void setProfile(StudentProfile profile) { this.profile = profile; }
        public CompanyProfile getCompanyProfile() { return companyProfile; }
        public void setCompanyProfile(CompanyProfile companyProfile) { this.companyProfile = companyProfile; }
        public TeacherProfile getTeacherProfile() { return teacherProfile; }
        public void setTeacherProfile(TeacherProfile teacherProfile) { this.teacherProfile = teacherProfile; }
    }
    
    static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    static class StudentProfile {
        private String firstName;
        private String lastName;
        private String university;
        private String graduationYear;
        private String[] skills;
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getUniversity() { return university; }
        public void setUniversity(String university) { this.university = university; }
        public String getGraduationYear() { return graduationYear; }
        public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
        public String[] getSkills() { return skills; }
        public void setSkills(String[] skills) { this.skills = skills; }
    }
    
    static class TeacherProfile {
        private String firstName;
        private String lastName;
        private String department;
        private String subject;
        private Integer experience;
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public Integer getExperience() { return experience; }
        public void setExperience(Integer experience) { this.experience = experience; }
    }
    
    static class CompanyProfile {
        private String companyName;
        private String email;
        private String sector;
        private String address;
        private String website;
        private String description;
        private String profilePhoto;
        
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSector() { return sector; }
        public void setSector(String sector) { this.sector = sector; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getProfilePhoto() { return profilePhoto; }
        public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    }
    
    static class AuthResponse {
        private boolean success;
        private Long id;
        private String email;
        private String role;
        private String firstName;
        private String lastName;
        
        public AuthResponse(boolean success, Long id, String email, String role) {
            this.success = success;
            this.id = id;
            this.email = email;
            this.role = role;
        }
        
        public AuthResponse(boolean success, Long id, String email, String role, String firstName, String lastName) {
            this.success = success;
            this.id = id;
            this.email = email;
            this.role = role;
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        public boolean isSuccess() { return success; }
        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }
    
    static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
