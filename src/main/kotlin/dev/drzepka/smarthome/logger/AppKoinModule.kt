package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.core.pipeline.PipelineRegistrar
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import org.koin.dsl.module

val appModule = module {
    single<ConfigPropertySource> { ConfigurationLoader().loadSource() }
    single { TaskScheduler(8) }
    single { PipelineManager(get(), get<DataSender>()) }
    single { PipelineRegistrar(get(), getAll<PipelineFactory>(), get()) }
}
