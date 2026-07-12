package com.assetflow.config;

import com.assetflow.maintenance.integration.AssetLifecycleGateway;
import com.assetflow.maintenance.integration.StubAssetLifecycleGateway;
import com.assetflow.technician.EmployeeLookupGateway;
import com.assetflow.technician.StubEmployeeLookupGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to provide fallback stub implementations for gateways.
 * These stubs are only active when no real implementation is provided
 * in the Spring context by the responsible members.
 */
@Slf4j
@Configuration
public class GatewayFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(AssetLifecycleGateway.class)
    public AssetLifecycleGateway stubAssetLifecycleGateway() {
        log.info("[CONFIG] Registering StubAssetLifecycleGateway as fallback");
        return new StubAssetLifecycleGateway();
    }

    @Bean
    @ConditionalOnMissingBean(EmployeeLookupGateway.class)
    public EmployeeLookupGateway stubEmployeeLookupGateway() {
        log.info("[CONFIG] Registering StubEmployeeLookupGateway as fallback");
        return new StubEmployeeLookupGateway();
    }
}
