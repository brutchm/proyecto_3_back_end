package com.project.demo.logic.entity.userfarm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFarmId implements Serializable {

    @Column(name = "farm_id")
    private Long farmId;

    @Column(name = "user_id")
    private Long userId;

    // Constructores, Getters, Setters, hashCode y equals son necesarios

    public UserFarmId() {}

    public UserFarmId(Long farmId, Long userId) {
        this.farmId = farmId;
        this.userId = userId;
    }

    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFarmId that = (UserFarmId) o;
        return Objects.equals(farmId, that.farmId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(farmId, userId);
    }
}