package com.beatrunner.database

import com.beatrunner.database.tables.Accounts
import com.beatrunner.database.tables.Identities
import com.beatrunner.database.tables.Profiles
import com.beatrunner.database.tables.Settings
import com.beatrunner.database.tables.UserDevices
import com.beatrunner.database.tables.WorkoutMusics
import com.beatrunner.database.tables.WorkoutSessions
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

import com.beatrunner.database.tables.WorkoutDataPoints

/** Database factory for managing PostgreSQL connections with HikariCP pooling. */
object DatabaseFactory {
    private val dotenv = dotenv { ignoreIfMissing = true }

    private val logger = org.slf4j.LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        logger.info("Initializing database connection...")
        val database = Database.connect(createHikariDataSource())

        transaction(database) {
            SchemaUtils.create(
                    Accounts,
                    Identities,
                    Profiles,
                    Settings,
                    UserDevices,
                    WorkoutSessions,
                    WorkoutMusics,
                    WorkoutDataPoints,
                    com.beatrunner.database.tables.Songs
            )
            // Migration: Add tags column if not exists
            exec("ALTER TABLE songs ADD COLUMN IF NOT EXISTS tags TEXT") { }
        }
        logger.info("Database initialized successfully.")
    }

    private fun createHikariDataSource(): HikariDataSource {
        val config =
                HikariConfig().apply {
                    jdbcUrl =
                            "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DB_NAME"]}"
                    driverClassName = "org.postgresql.Driver"
                    username = dotenv["DB_USER"]
                    password = dotenv["DB_PASSWORD"]
                    maximumPoolSize = 10
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                    validate()
                }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
            newSuspendedTransaction(Dispatchers.IO) { block() }
}
