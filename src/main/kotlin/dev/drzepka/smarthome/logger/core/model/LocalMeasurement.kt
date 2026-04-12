package dev.drzepka.smarthome.logger.core.model

import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement

data class LocalMeasurement(val mac: MacAddress, val measurement: Measurement)
