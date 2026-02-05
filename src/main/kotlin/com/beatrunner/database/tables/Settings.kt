package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** Settings table schema. Stores user preferences in JSON format for flexibility. */
object Settings : Table("settings") {
    val accountId = uuid("account_id").references(Accounts.id, onDelete = ReferenceOption.CASCADE)

    // JSON 格式存储灵活配置
    // 示例结构: {
    //   "unitSystem": "metric",
    //   "audioGuide": true,
    //   "safetyLockSpeed": 12.0,
    //   "warmupDuration": 180,
    //   "musicSyncMode": "cadence",
    //   "privacyConsentVersion": "1.0"
    // }
    val preferences = text("preferences").default("{}")

    override val primaryKey = PrimaryKey(accountId)
}
