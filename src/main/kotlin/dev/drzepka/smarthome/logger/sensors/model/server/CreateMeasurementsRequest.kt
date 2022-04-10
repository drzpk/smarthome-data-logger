package dev.drzepka.smarthome.logger.sensors.model.server

import java.io.Serializable

class CreateMeasurementsRequest : Serializable {

    var measurements = ArrayList<Measurement>()

}