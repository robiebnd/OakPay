package com.oakpay.auth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false, unique = true, length = 320) private String email;
    @Column(nullable = false) private String password;
    @Column(nullable = false, length = 100) private String firstName;
    @Column(nullable = false, length = 100) private String lastName;
    @Column(length = 30) private String phoneNumber;
    @Column(length = 100) private String country;
    private LocalDate dateOfBirth;
    @Column(nullable = false) private boolean enabled = true;
    @Column(nullable = false) private boolean emailVerified = false;
    @Column(nullable = false) private boolean twoFactorEnabled = false;
    @Column(length = 512) private String twoFactorSecretEncrypted;
    @Column(nullable = false, length = 30) private String role = "CLIENT";
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); if(role==null||role.isBlank())role="CLIENT"; LocalDateTime now=LocalDateTime.now(); if(createdAt==null)createdAt=now; if(updatedAt==null)updatedAt=now; }
    @PreUpdate void preUpdate() { updatedAt=LocalDateTime.now(); }
    public UUID getId(){return id;} public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
    public String getFirstName(){return firstName;} public void setFirstName(String firstName){this.firstName=firstName;}
    public String getLastName(){return lastName;} public void setLastName(String lastName){this.lastName=lastName;}
    public String getPhoneNumber(){return phoneNumber;} public void setPhoneNumber(String phoneNumber){this.phoneNumber=phoneNumber;}
    public String getCountry(){return country;} public void setCountry(String country){this.country=country;}
    public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate dateOfBirth){this.dateOfBirth=dateOfBirth;}
    public boolean isEnabled(){return enabled;} public boolean isEmailVerified(){return emailVerified;} public void setEmailVerified(boolean emailVerified){this.emailVerified=emailVerified;}
    public boolean isTwoFactorEnabled(){return twoFactorEnabled;} public void setTwoFactorEnabled(boolean twoFactorEnabled){this.twoFactorEnabled=twoFactorEnabled;}
    public String getTwoFactorSecretEncrypted(){return twoFactorSecretEncrypted;} public void setTwoFactorSecretEncrypted(String twoFactorSecretEncrypted){this.twoFactorSecretEncrypted=twoFactorSecretEncrypted;}
    public String getRole(){return role;} public void setRole(String role){this.role=role;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}