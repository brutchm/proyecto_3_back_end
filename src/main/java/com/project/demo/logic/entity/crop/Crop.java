package com.project.demo.logic.entity.crop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * @class Crop
 * @description
 * Entidad que representa un cultivo personalizado creado por un usuario.
 * Cada registro de cultivo está directamente asociado a un FarmAdmin específico.
 */
@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "crop_name", nullable = false)
    private String cropName;

    @Column(name = "crop_picture")
    private String cropPicture;

    @Column(name = "crop_type", length = 100)
    private String cropType;

    @Column(name = "crop_variety", length = 100)
    private String cropVariety;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isActive")
    private Boolean isActive = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public String getCropPicture() { return cropPicture; }
    public void setCropPicture(String cropPicture) { this.cropPicture = cropPicture; }
    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }
    public String getCropVariety() { return cropVariety; }
    public void setCropVariety(String cropVariety) { this.cropVariety = cropVariety; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean active) {
        isActive = active;
    }
}