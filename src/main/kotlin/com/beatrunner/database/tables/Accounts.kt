package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/** Accounts table schema. Stores core account information, separated from business attributes. */
object Accounts : Table("accounts") {
    val id = uuid("id").autoGenerate()
    val nickname = varchar("nickname", 50).nullable()
    val avatarUrl = text("avatar_url").nullable()

    // 账户状态
    val status = varchar("status", 20).default("active") // active, banned, deleted
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val deletedAt = datetime("deleted_at").nullable() // 软删除

    override val primaryKey = PrimaryKey(id)
}
