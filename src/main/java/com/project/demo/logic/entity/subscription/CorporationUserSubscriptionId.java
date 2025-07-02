package com.project.demo.logic.entity.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CorporationUserSubscriptionId implements Serializable {

    @Column(name = "corporation_user_id")
    private Integer corporationUserId;

    @Column(name = "individual_user_id")
    private Integer individualUserId;

    // Constructores, Getters, Setters, hashCode, y equals

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

    public Integer getCorporationUserId() {
        return corporationUserId;
    }

    public void setCorporationUserId(Integer corporationUserId) {
        this.corporationUserId = corporationUserId;
    }

    public Integer getIndividualUserId() {
        return individualUserId;
    }

    public void setIndividualUserId(Integer individualUserId) {
        this.individualUserId = individualUserId;
    }
}