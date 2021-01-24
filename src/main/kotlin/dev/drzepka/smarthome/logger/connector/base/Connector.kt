package dev.drzepka.smarthome.logger.connector.base

import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData

/**
 * Retrieves data from inverter
 */
interface Connector {
    val supportedDataTypes: List<DataType>

    fun initialize()
    fun getData(dataType: DataType, silent: Boolean): VendorData?
    fun getUrl(dataType: DataType): String
}