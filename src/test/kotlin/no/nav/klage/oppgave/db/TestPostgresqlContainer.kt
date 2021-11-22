package no.nav.klage.oppgave.db

import org.testcontainers.containers.PostgreSQLContainer

class TestPostgresqlContainer private constructor() :
    PostgreSQLContainer<TestPostgresqlContainer?>(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "postgres:12.6"

        private val CONTAINER: TestPostgresqlContainer = TestPostgresqlContainer()

        val instance: TestPostgresqlContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        super.start()
        System.setProperty("DB_URL", CONTAINER.jdbcUrl)
        System.setProperty("DB_USERNAME", CONTAINER.username)
        System.setProperty("DB_PASSWORD", CONTAINER.password)
    }

    override fun stop() {
        //do nothing, JVM handles shut down
    }


}