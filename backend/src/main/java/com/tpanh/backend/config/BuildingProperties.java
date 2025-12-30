package com.tpanh.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.building")
@Data
public class BuildingProperties {
    private int defaultElecUnitPrice = 3500;
    private int defaultWaterUnitPrice = 20000;
}
