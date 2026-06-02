package com.careertrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String firstName;
    private String lastName;
    private String university;
    private String graduationYear;
    private String[] skills;
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "profile_photo")
    private String profilePhoto;

    private String degree;
    @Column(name = "diploma_image")
    private String diplomaImage;

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getDiplomaImage() { return diplomaImage; }
    public void setDiplomaImage(String diplomaImage) { this.diplomaImage = diplomaImage; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}