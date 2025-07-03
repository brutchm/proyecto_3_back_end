package com.project.demo.logic.entity.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CorporationUserSubscriptionId implements Serializable {

    @Column(name = "corporation_user_id")
    private Long corporationUserId;

    @Column(name = "individual_user_id")
    private Long individualUserId;

    // Constructores, Getters, Setters, hashCode, y equals


    public CorporationUserSubscriptionId(Long corporationUserId, Long individualUserId) {
        this.corporationUserId = corporationUserId;
        this.individualUserId = individualUserId;
    }

    public CorporationUserSubscriptionId() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorporationUserSubscriptionId that = (CorporationUserSubscriptionId) o;
        return Objects.equals(corporationUserId, that.corporationUserId) && Objects.equals(individualUserId, that.individualUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(corporationUserId, individualUserId);
    }

    public Long getCorporationUserId() {
        return corporationUserId;
    }

    public void setCorporationUserId(Long corporationUserId) {
        this.corporationUserId = corporationUserId;
    }

    public Long getIndividualUserId() {
        return individualUserId;
    }

    public void setIndividualUserId(Long individualUserId) {
        this.individualUserId = individualUserId;
    }
}