package dev.drzepka.smarthome.logger.core.model.measurement

data class CreateMeasurementsRequest(
    var measurements: List<Measurement>
)

class CreateMeasurementsResponse {
    var created = 0
    var duplicated = 0
    var errors = 0
    var total = 0
}

interface Measurement {
    val type: MeasurementType
    val mac: String
}

enum class MeasurementType {
    TEMPERATURE, PV
}
