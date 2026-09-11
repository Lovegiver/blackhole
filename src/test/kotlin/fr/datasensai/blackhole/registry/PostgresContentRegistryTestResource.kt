package fr.datasensai.blackhole.registry

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.flywaydb.core.Flyway
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

/** Creates isolated migrator/runtime roles on the same PostgreSQL version as Lab-IA DEV. */
class PostgresContentRegistryTestResource : QuarkusTestResourceLifecycleManager {

    private lateinit var postgres: PostgreSQLContainer

    override fun start(): Map<String, String> {
        postgres = PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName(ADMIN_DATABASE)
            .withUsername(ADMIN_USER)
            .withPassword(ADMIN_PASSWORD)
        postgres.start()

        DriverManager.getConnection(postgres.jdbcUrl, ADMIN_USER, ADMIN_PASSWORD).use { connection ->
            connection.autoCommit = true
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    "CREATE ROLE $MIGRATION_USER LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE " +
                        "NOINHERIT NOREPLICATION NOBYPASSRLS PASSWORD '$MIGRATION_PASSWORD'"
                )
                statement.executeUpdate(
                    "CREATE ROLE $RUNTIME_USER LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE " +
                        "NOINHERIT NOREPLICATION NOBYPASSRLS PASSWORD '$RUNTIME_PASSWORD'"
                )
                statement.executeUpdate("CREATE DATABASE $APPLICATION_DATABASE OWNER $MIGRATION_USER")
                statement.executeUpdate("REVOKE ALL ON DATABASE $APPLICATION_DATABASE FROM PUBLIC")
                statement.executeUpdate(
                    "GRANT CONNECT, TEMPORARY ON DATABASE $APPLICATION_DATABASE TO $MIGRATION_USER"
                )
                statement.executeUpdate("GRANT CONNECT ON DATABASE $APPLICATION_DATABASE TO $RUNTIME_USER")
                statement.executeUpdate(
                    "ALTER ROLE $MIGRATION_USER IN DATABASE $APPLICATION_DATABASE " +
                        "SET search_path = blackhole, pg_catalog"
                )
                statement.executeUpdate(
                    "ALTER ROLE $RUNTIME_USER IN DATABASE $APPLICATION_DATABASE " +
                        "SET search_path = blackhole, pg_catalog"
                )
            }
        }

        Flyway.configure()
            .dataSource(applicationJdbcUrl(), MIGRATION_USER, MIGRATION_PASSWORD)
            .schemas("blackhole")
            .defaultSchema("blackhole")
            .createSchemas(true)
            .cleanDisabled(true)
            .placeholders(mapOf("runtimeRole" to RUNTIME_USER))
            .load()
            .migrate()

        return mapOf(
            "quarkus.datasource.jdbc.url" to applicationJdbcUrl(),
            "quarkus.datasource.username" to RUNTIME_USER,
            "quarkus.datasource.password" to RUNTIME_PASSWORD,
            "quarkus.datasource.devservices.enabled" to "false",
            "quarkus.flyway.migrate-at-start" to "false"
        )
    }

    override fun stop() {
        if (::postgres.isInitialized) {
            postgres.stop()
        }
    }

    private fun applicationJdbcUrl(): String =
        postgres.jdbcUrl.replace("/$ADMIN_DATABASE", "/$APPLICATION_DATABASE")

    private companion object {
        val POSTGRES_IMAGE: DockerImageName = DockerImageName.parse(
            "postgres:18.6@sha256:7341002d2b8c7c5bdd7542a671a95b36196c0b5b888daf454ae4fc33ba5346d7"
        ).asCompatibleSubstituteFor("postgres")

        const val ADMIN_DATABASE = "postgres"
        const val ADMIN_USER = "blackhole_test_admin"
        const val ADMIN_PASSWORD = "test-admin-password"
        const val APPLICATION_DATABASE = "blackhole_test"
        const val MIGRATION_USER = "blackhole_test_migrator"
        const val MIGRATION_PASSWORD = "test-migration-password"
        const val RUNTIME_USER = "blackhole_test_runtime"
        const val RUNTIME_PASSWORD = "test-runtime-password"
    }
}
