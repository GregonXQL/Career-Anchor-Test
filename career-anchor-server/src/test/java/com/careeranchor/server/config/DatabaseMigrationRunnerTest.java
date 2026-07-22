package com.careeranchor.server.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseMigrationRunnerTest {
    @Test
    void migratesDatabaseWhenApplicationRunnerStarts() throws Exception {
        @SuppressWarnings("unchecked")
        ObjectProvider<Flyway> provider = mock(ObjectProvider.class);
        Flyway flyway = mock(Flyway.class);
        when(provider.getObject()).thenReturn(flyway);

        new DatabaseMigrationRunner(provider).run(null);

        verify(flyway).migrate();
    }
}
