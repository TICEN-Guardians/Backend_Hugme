package com.project.hugme.global.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MainFlywayConfig {

    @Bean(name = "mainFlyway", initMethod = "migrate")
    public Flyway mainFlyway(
            @Qualifier("mainDataSource") DataSource mainDataSource
    ) {
        return Flyway.configure()
                .dataSource(mainDataSource)
                .locations("classpath:db/migration/rds")
                .baselineOnMigrate(true)
                .load();
    }
}