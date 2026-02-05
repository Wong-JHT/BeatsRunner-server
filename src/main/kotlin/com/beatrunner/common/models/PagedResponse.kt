package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/** Generic paged response. */
@Serializable
data class PagedResponse<T>(
        val data: List<T>,
        val page: Int,
        val pageSize: Int,
        val totalCount: Long,
        val totalPages: Int
)
