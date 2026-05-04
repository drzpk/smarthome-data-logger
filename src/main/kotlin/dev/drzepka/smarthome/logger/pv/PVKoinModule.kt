package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.pv.source.afore.AforeT6PipelineFactory
import dev.drzepka.smarthome.logger.pv.source.sma.SmaPipelineFactory
import dev.drzepka.smarthome.logger.pv.source.sofar.SofarModbusPipelineFactory
import dev.drzepka.smarthome.logger.pv.source.sofar.SofarWifiPipelineFactory
import org.koin.dsl.bind
import org.koin.dsl.module

val pvModule = module {
    single { AforeT6PipelineFactory() } bind PipelineFactory::class
    single { SofarWifiPipelineFactory() } bind PipelineFactory::class
    single { SofarModbusPipelineFactory() } bind PipelineFactory::class
    single { SmaPipelineFactory() } bind PipelineFactory::class
    single { PipelineModule(get(), get()) } bind DataLoggerModule::class
}
