package dev.drzepka.smarthome.logger.sensors.model

import dev.drzepka.smarthome.logger.sensors.model.server.Measurement

data class LocalMeasurement(val mac: MacAddress, val measurement: Measurement)
