package com.project.demo.rest.dashboard;

/**
 * Enum para definir los tipos de reportes válidos en la aplicación.
 * Esto proporciona seguridad de tipos y evita el uso de "magic strings".
 */
public enum ReportTypeEnum {
    INCOME_VS_EXPENSES,
    CROP_YIELD,
    OPERATIONAL_COSTS,
    PLOT_YIELD,
    CROP_COSTS,
    FARM_COSTS,
}
