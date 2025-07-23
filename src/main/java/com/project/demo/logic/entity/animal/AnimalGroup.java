package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.user.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animal_groups")
public class AnimalGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    @JsonBackReference
    private Farm farm;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "species", length = 100)
    private String species;

    @Column(name = "count")
    private Integer count;

    @Column(name = "measure", length = 50)
    private String measure;

    @Convert(converter = ProductionTypeEnumConverter.class)
    @Column(name = "production_type")
    private ProductionTypeEnum productionType;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    @OneToMany(mappedBy = "animalGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Animal> animals = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public String getMeasure() { return measure; }
    public void setMeasure(String measure) { this.measure = measure; }
    public ProductionTypeEnum getProductionType() { return productionType; }
    public void setProductionType(ProductionTypeEnum productionType) { this.productionType = productionType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }    public List<Animal> getAnimals() {
        return animals;
    }
    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }

}