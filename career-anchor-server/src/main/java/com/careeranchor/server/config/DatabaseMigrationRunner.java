package com.careeranchor.server.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class DatabaseMigrationRunner implements ApplicationRunner {
    private final ObjectProvider<Flyway> flywayProvider;

    public DatabaseMigrationRunner(ObjectProvider<Flyway> flywayProvider) {
        this.flywayProvider = flywayProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        flywayProvider.getObject().migrate();
    }
}
