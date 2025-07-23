package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductionTypeEnum {
    CARNE,
    LECHE,
    HUEVOS,
    DERIVADOS;

    @JsonCreator
    public static ProductionTypeEnum fromValue(String value) {
        return ProductionTypeEnum.valueOf(value.toUpperCase());
    }
}