package com.project.demo.logic.entity.transaction;

public enum MeasureUnitEnum {
    KILOGRAMO("Kilogramo (kg)"),
    GRAMO("Gramo (g)"),
    LITRO("Litro (L)"),
    MILILITRO("Mililitro (mL)"),
    TONELADA("Tonelada (t)"),
    SACO("Saco"),
    BOLSA("Bolsa"),
    CAJA("Caja"),
    UNIDAD("Unidad");

    private final String displayName;

    MeasureUnitEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
