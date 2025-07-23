package com.project.demo.logic.entity.animal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductionTypeEnumConverter implements AttributeConverter<ProductionTypeEnum, String> {
    @Override
    public String convertToDatabaseColumn(ProductionTypeEnum attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public ProductionTypeEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return ProductionTypeEnum.valueOf(dbData.toUpperCase());
    }
}
