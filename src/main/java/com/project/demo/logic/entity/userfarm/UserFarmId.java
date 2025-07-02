package com.project.demo.logic.entity.userfarm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFarmId implements Serializable {

    @Column(name = "farm_id")
    private Integer farmId;

    @Column(name = "user_id")
    private Integer userId;

    // Constructores, Getters, Setters, hashCode y equals son necesarios

    public UserFarmId() {}

    public UserFarmId(Integer farmId, Integer userId) {
        this.farmId = farmId;
        this.userId = userId;
    }

    public Integer getFarmId() { return farmId; }
    public void setFarmId(Integer farmId) { this.farmId = farmId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

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