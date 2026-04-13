package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import org.koin.dsl.module

val appModule = module {
    single<ConfigPropertySource> { ConfigurationLoader().loadSource() }
    single { TaskScheduler(8) }
    single { PipelineManager(get()) }
}
