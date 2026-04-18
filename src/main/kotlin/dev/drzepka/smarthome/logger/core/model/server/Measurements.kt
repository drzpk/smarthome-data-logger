package dev.drzepka.smarthome.logger.core.model.server

import dev.drzepka.smarthome.logger.sensors.model.server.Measurement

class CreateMeasurementsRequest {
    var measurements = ArrayList<Measurement>()
}

class CreateMeasurementsResponse {
    var created = 0
    var duplicated = 0
    var errors = 0
    var total = 0
}

interface Measurement {
    val type: MeasurementType
    val deviceId: Int
}

enum class MeasurementType {
    TEMPERATURE, PV
}
