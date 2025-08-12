package com.project.demo.logic.entity.farm;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.demo.logic.entity.animal.AnimalGroup;
import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.userfarm.UserXFarm;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "farms")

public class Farm {
    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AnimalGroup> animalGroups = new ArrayList<>();

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<UserXFarm> userLinks;

    public List<AnimalGroup> getAnimalGroups() {
        return animalGroups;
    }

    public void setAnimalGroups(List<AnimalGroup> animalGroups) {
        this.animalGroups = animalGroups;
    }

    public Set<UserXFarm> getUserLinks() {
        return userLinks;
    }

    public void setUserLinks(Set<UserXFarm> userLinks) {
        this.userLinks = userLinks;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(name = "farm_country", length = 100)
    private String farmCountry;

    @Column(name = "farm_state_province", length = 100)
    private String farmStateProvince;

    @Lob
    @Column(name = "farm_other_directions", columnDefinition = "TEXT")
    private String farmOtherDirections;

    @Column(name = "farm_location")
    private String farmLocation;

    @Column(name = "farm_size")
    private Double farmSize;

    @Column(name = "farm_measure_unit", length = 50)
    private String farmMeasureUnit;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    @OneToOne(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("farm-technicalInfo")
    private FarmsTechnicalInformation technicalInformation;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FarmPlot> plots = new ArrayList<>();

    public List<FarmPlot> getPlots() { return plots; }
    public void setPlots(List<FarmPlot> plots) { this.plots = plots; }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getFarmCountry() { return farmCountry; }
    public void setFarmCountry(String farmCountry) { this.farmCountry = farmCountry; }
    public String getFarmStateProvince() { return farmStateProvince; }
    public void setFarmStateProvince(String farmStateProvince) { this.farmStateProvince = farmStateProvince; }
    public String getFarmOtherDirections() { return farmOtherDirections; }
    public void setFarmOtherDirections(String farmOtherDirections) { this.farmOtherDirections = farmOtherDirections; }
    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }
    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }
    public String getFarmMeasureUnit() { return farmMeasureUnit; }
    public void setFarmMeasureUnit(String farmMeasureUnit) { this.farmMeasureUnit = farmMeasureUnit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public FarmsTechnicalInformation getTechnicalInformation() {
        return technicalInformation;
    }

    public void setTechnicalInformation(FarmsTechnicalInformation technicalInformation) {
        this.technicalInformation = technicalInformation;
    }

}