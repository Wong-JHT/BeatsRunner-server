package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Identities table schema. Supports multi-identity authentication: email, phone, apple, wechat. One
 * account can have multiple identities.
 */
object Identities : Table("identities") {
    val id = long("id").autoIncrement()
    val accountId = uuid("account_id").references(Accounts.id, onDelete = ReferenceOption.CASCADE)

    // 认证类型: email, phone, apple, wechat
    val identityType = varchar("identity_type", 20)

    // 唯一标识: 邮箱地址、手机号、Apple Sub ID、微信 OpenID/UnionID
    val identifier = varchar("identifier", 255)

    // 凭证: 密码Hash(仅限email/phone)，第三方登录此字段可为空
    val credential = varchar("credential", 255).nullable()

    // 第三方登录扩展信息 (UnionID, AccessToken等) - JSON 格式
    val extraData = text("extra_data").nullable()

    val lastLoginAt = datetime("last_login_at").nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
