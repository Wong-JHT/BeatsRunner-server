package com.beatrunner.database.tables

import java.math.BigDecimal
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Profiles table schema. Core algorithm data source for physiological and running ability
 * parameters.
 */
object Profiles : Table("profiles") {
    val accountId = uuid("account_id").references(Accounts.id, onDelete = ReferenceOption.CASCADE)

    // Updated schema matching UserProfile
    val age = integer("age").nullable()
    val weight = float("weight").nullable() // kg
    val height = float("height").nullable() // cm
    val fitnessLevel = varchar("fitness_level", 20).nullable() // Store enum as string
    val targetHeartRate = integer("target_heart_rate").nullable()

    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(accountId)
}
