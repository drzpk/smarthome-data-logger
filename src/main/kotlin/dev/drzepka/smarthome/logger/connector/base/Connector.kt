package dev.drzepka.smarthome.logger.connector.base

import dev.drzepka.smarthome.logger.model.config.SourceConfig
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData

/**
 * Retrieves data from inverter
 */
interface Connector {
    val supportedDataTypes: List<DataType>

    fun initialize(config: SourceConfig)
    fun getData(config: SourceConfig, dataType: DataType, silent: Boolean): VendorData?
    fun getUrl(config: SourceConfig, dataType: DataType): String
}