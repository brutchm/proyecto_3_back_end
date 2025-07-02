package com.project.demo.logic.entity.corporation;

import com.project.demo.logic.entity.role.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "corporations")
public class Corporation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "corporation_name", nullable = false, length = 255)
    private String corporationName;

    @Column(name = "corporation_country", length = 100)
    private String corporationCountry;

    @Column(name = "corporation_state_province", length = 100)
    private String corporationStateProvince;

    @Column(name = "corporation_other", columnDefinition = "TEXT")
    private String corporationOther;

    @Column(name = "corporation_location", length = 255)
    private String corporationLocation;

    @Column(name = "corporation_email", length = 150)
    private String corporationEmail;

    @Column(name = "corporation_password", length = 255)
    private String corporationPassword;

    @Column(name = "corporation_mision", columnDefinition = "TEXT")
    private String corporationMision;

    @Column(name = "corporation_vision", columnDefinition = "TEXT")
    private String corporationVision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role")
    private Role role;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive")
    private Boolean isActive;

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getCorporationCountry() {
        return corporationCountry;
    }

    public void setCorporationCountry(String corporationCountry) {
        this.corporationCountry = corporationCountry;
    }

    public String getCorporationStateProvince() {
        return corporationStateProvince;
    }

    public void setCorporationStateProvince(String corporationStateProvince) {
        this.corporationStateProvince = corporationStateProvince;
    }

    public String getCorporationOther() {
        return corporationOther;
    }

    public void setCorporationOther(String corporationOther) {
        this.corporationOther = corporationOther;
    }

    public String getCorporationLocation() {
        return corporationLocation;
    }

    public void setCorporationLocation(String corporationLocation) {
        this.corporationLocation = corporationLocation;
    }

    public String getCorporationEmail() {
        return corporationEmail;
    }

    public void setCorporationEmail(String corporationEmail) {
        this.corporationEmail = corporationEmail;
    }

    public String getCorporationPassword() {
        return corporationPassword;
    }

    public void setCorporationPassword(String corporationPassword) {
        this.corporationPassword = corporationPassword;
    }

    public String getCorporationMision() {
        return corporationMision;
    }

    public void setCorporationMision(String corporationMision) {
        this.corporationMision = corporationMision;
    }

    public String getCorporationVision() {
        return corporationVision;
    }

    public void setCorporationVision(String corporationVision) {
        this.corporationVision = corporationVision;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}