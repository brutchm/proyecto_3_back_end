package com.project.demo.logic.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.role.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_mission", columnDefinition = "TEXT")
    private String businessMission;

    @Column(name = "business_vision", columnDefinition = "TEXT")
    private String businessVision;

    @Column(name = "business_id", unique = true, length = 100)
    private String businessId;

    @Column(name = "business_country", length = 100)
    private String businessCountry;

    @Column(name = "business_state_province", length = 100)
    private String businessStateProvince;

    @Column(name = "business_other_directions", columnDefinition = "TEXT")
    private String businessOtherDirections;

    @Column(name = "business_location", length = 255)
    private String businessLocation;

    @Column(name = "user_name", length = 100)
    private String name;

    @Column(name = "user_first_surename", length = 100)
    private String userFirstSurename;

    @Column(name = "user_second_surename", length = 100)
    private String userSecondSurename;

    @Column(name = "user_gender", length = 20)
    private String userGender;

    @Column(name = "user_phone_number", length = 50)
    private String userPhoneNumber;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "user_password", length = 255)
    private String userPassword;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role")
    private Role role;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isActive")
    private Boolean isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toString());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessMission() {
        return businessMission;
    }

    public void setBusinessMission(String businessMission) {
        this.businessMission = businessMission;
    }

    public String getBusinessVision() {
        return businessVision;
    }

    public void setBusinessVision(String businessVision) {
        this.businessVision = businessVision;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessCountry() {
        return businessCountry;
    }

    public void setBusinessCountry(String businessCountry) {
        this.businessCountry = businessCountry;
    }

    public String getBusinessStateProvince() {
        return businessStateProvince;
    }

    public void setBusinessStateProvince(String businessStateProvince) {
        this.businessStateProvince = businessStateProvince;
    }

    public String getBusinessOtherDirections() {
        return businessOtherDirections;
    }

    public void setBusinessOtherDirections(String businessOtherDirections) {
        this.businessOtherDirections = businessOtherDirections;
    }

    public String getBusinessLocation() {
        return businessLocation;
    }

    public void setBusinessLocation(String businessLocation) {
        this.businessLocation = businessLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String userName) {
        this.name = userName;
    }

    public String getUserFirstSurename() {
        return userFirstSurename;
    }

    public void setUserFirstSurename(String userFirstSurename) {
        this.userFirstSurename = userFirstSurename;
    }

    public String getUserSecondSurename() {
        return userSecondSurename;
    }

    public void setUserSecondSurename(String userSecondSurename) {
        this.userSecondSurename = userSecondSurename;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
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
