package com.project.demo.logic.entity.subscription;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "corporations_x_users")
public class CorporationUserSubscription {

    @EmbeddedId
    private CorporationUserSubscriptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("corporationUserId")
    @JoinColumn(name = "corporation_user_id")
    private User corporation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("individualUserId")
    @JoinColumn(name = "individual_user_id")
    private User individualUser;

    @Column(name = "isActive_subscription", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActiveSubscription;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Getters y Setters

    public CorporationUserSubscriptionId getId() {
        return id;
    }

    public void setId(CorporationUserSubscriptionId id) {
        this.id = id;
    }

    public User getCorporation() {
        return corporation;
    }

    public void setCorporation(User corporation) {
        this.corporation = corporation;
    }

    public User getIndividualUser() {
        return individualUser;
    }

    public void setIndividualUser(User individualUser) {
        this.individualUser = individualUser;
    }

    public boolean isActiveSubscription() {
        return isActiveSubscription;
    }

    public void setActiveSubscription(boolean activeSubscription) {
        isActiveSubscription = activeSubscription;
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
}