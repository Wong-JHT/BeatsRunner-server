package com.beatrunner.database.tables

import java.math.BigDecimal
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * UserDevices table schema. Records treadmill/bike device capabilities to prevent exceeding
 * hardware limits.
 */
object UserDevices : Table("user_devices") {
    val id = uuid("id").autoGenerate()
    val accountId = uuid("account_id").references(Accounts.id, onDelete = ReferenceOption.CASCADE)

    val deviceName = varchar("device_name", 100) // e.g., "诺德士 T12"
    val deviceType = varchar("device_type", 20).default("treadmill") // treadmill, bike

    // FTMS 能力限制
    val maxSpeedKmh = decimal("max_speed_kmh", 4, 1).default(BigDecimal("20.0"))
    val maxInclinePercent = decimal("max_incline_percent", 3, 1).default(BigDecimal("15.0"))

    val isDefault = bool("is_default").default(false) // 是否为默认设备
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
