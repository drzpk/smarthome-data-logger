package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement

class MockLoggingSender : DataSender {

    init {
        logger.warn("Mock logging sender is enabled. Data will only be logged and not sent to the server. Configure ")
    }

    override fun queue(items: Collection<Measurement>) {
        logger.info("###  Queueing ${items.size} items ###")
        items.forEachIndexed { index, item ->
            logger.info("Item #{}: {}", index + 1, item)
        }
    }

    override suspend fun send(items: Collection<Measurement>) {
        logger.info("###  Sending ${items.size} items ###")
        items.forEachIndexed { index, item ->
            logger.info("Item #{}: {}", index + 1, item)
        }
    }

    companion object {
        private val logger by Logger()
    }
}
