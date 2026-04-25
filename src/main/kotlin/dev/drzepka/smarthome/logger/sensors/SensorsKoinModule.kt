package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.logger.sensors.source.shtc3.SHTC3PipelineFactory
import dev.drzepka.smarthome.logger.sensors.source.xiaomimijia.XiaomiMijiaPipelineFactory
import org.koin.dsl.module

val sensorsModule = module {
    single { XiaomiMijiaPipelineFactory() }
    single { SHTC3PipelineFactory() }
}
