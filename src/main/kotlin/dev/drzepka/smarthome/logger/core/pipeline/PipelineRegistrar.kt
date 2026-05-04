package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.SourceType

class PipelineRegistrar(
    private val config: ConfigPropertySource,
    private val factories: List<PipelineFactory>,
    private val pipelineManager: PipelineManager
) {
    private val log by Logger()

    fun registerAll() {
        validateNoDuplicateFactories()

        val factoryMap = factories.associateBy { it.sourceType }
        val sourceConfig = config.getChild("source")

        for (name in config.getKeys("source")) {
            registerSource(name, sourceConfig.getChild(name), factoryMap)
        }
    }

    private fun validateNoDuplicateFactories() {
        val duplicates = factories.groupBy { it.sourceType }.filter { it.value.size > 1 }
        if (duplicates.isEmpty())
            return

        duplicates.forEach { (type, dupes) ->
            log.error(
                "Multiple pipeline factories registered for source type {}: {}",
                type,
                dupes.map { it::class.simpleName }
            )
        }
        throw IllegalStateException("Duplicate pipeline factories detected for types: ${duplicates.keys}")
    }

    private fun registerSource(
        name: String,
        props: ConfigPropertySource,
        factoryMap: Map<SourceType, PipelineFactory>
    ) {
        val typeName = props.getString("type")
        val sourceType = SourceType.entries.find { it.name.equals(typeName, ignoreCase = true) }
        if (sourceType == null) {
            log.error("Unknown source type '{}' for source '{}'", typeName, name)
            throw IllegalStateException("Unknown source type '$typeName' for source '$name'")
        }

        val factory = factoryMap[sourceType]
        if (factory == null) {
            log.error("No pipeline factory registered for source type '{}' (source: '{}')", sourceType, name)
            throw IllegalStateException("No pipeline factory registered for source type '$sourceType'")
        }

        val enabled = props.getBoolean("enabled", default = true)
        if (!enabled) {
            log.info("Source '{}' is disabled, skipping", name)
            return
        }

        val pipeline = factory.create(name, props) ?: return
        pipelineManager.addPipeline(pipeline)
    }
}
