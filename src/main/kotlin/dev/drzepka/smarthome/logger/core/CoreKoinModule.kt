package dev.drzepka.smarthome.logger.core

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.core.config.ServerProperties
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.PipelineManager
import dev.drzepka.smarthome.logger.core.pipeline.PipelineRegistrar
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.MockLoggingSender
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.ServerDataSender
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single<ConfigPropertySource> { ConfigurationLoader().loadSource() }
    singleOf(::ServerProperties)

    single { TaskScheduler(8) }
    single { PipelineManager(get(), get<DataSender>()) }
    single { PipelineRegistrar(get(), getAll<PipelineFactory>(), get()) }
    single { ServerRequestExecutor(get<ServerProperties>()) }

    single<DataSender> {
        val props = get<ServerProperties>()
        if (!props.mock)
            ServerDataSender(get())
        else
            MockLoggingSender()
    }
}