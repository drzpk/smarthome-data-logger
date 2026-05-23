package dev.drzepka.smarthome.logger.pv.source.sma

data class SmaField(
    val name: String,
    val key: String,
    val keyIdx: Int = 0,
    val factor: Int? = null,
    val unit: String? = null
)
