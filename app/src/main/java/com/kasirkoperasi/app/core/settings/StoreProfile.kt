package com.kasirkoperasi.app.core.settings

data class StoreProfile(
    val storeName: String = DEFAULT_STORE_NAME,
    val logoUri: String? = null,
)

const val DEFAULT_STORE_NAME = "KasirKoperasi"
