package dev.drzepka.smarthome.logger.core.pipeline.component

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement

interface DataFilter {
    fun filter(data: Measurement): Measurement?
}
