package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement

interface DataReceiver {
    fun onDataAvailable(items: Collection<Measurement>)
}
