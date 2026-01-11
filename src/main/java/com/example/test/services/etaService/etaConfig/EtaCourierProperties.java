package com.example.test.services.etaService.etaConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eta.courier")
@Getter
@Setter
public class EtaCourierProperties {

    private int baseTimeMinutes;

    private int noCourierMultiplier;
}
