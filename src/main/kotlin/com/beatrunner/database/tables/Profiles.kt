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

    // 基础生理数据
    val gender = char("gender").nullable() // M, F, O
    val birthday = date("birthday").nullable()
    val heightCm = decimal("height_cm", 5, 2).nullable()
    val weightKg = decimal("weight_kg", 5, 2).nullable()

    // 进阶运动数据（算法核心）
    val restingHeartRate = integer("resting_heart_rate").default(60) // 静息心率
    val maxHeartRate = integer("max_heart_rate").nullable() // 最大心率（可为空，按220-age计算）

    // 跑步能力评估（核心参数）
    // 数值越高，同样 BPM 的歌曲给出的速度越快
    val runningAbilityScore = decimal("running_ability_score", 4, 1).default(BigDecimal("30.0"))

    // 步态参数（直接影响 BPM 到 Speed 的转换）
    val strideLengthCm = decimal("stride_length_cm", 5, 2).nullable() // 为空时按身高*0.415估算

    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(accountId)
}
