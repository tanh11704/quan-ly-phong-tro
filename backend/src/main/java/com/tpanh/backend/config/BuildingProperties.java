package com.tpanh.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.building")
@Data
public class BuildingProperties {
    private static final int DEFAULT_ELEC_PRICE = 3500;
    private static final int DEFAULT_WATER_PRICE = 20000;

    private int defaultElecUnitPrice = DEFAULT_ELEC_PRICE;
    private int defaultWaterUnitPrice = DEFAULT_WATER_PRICE;
}
