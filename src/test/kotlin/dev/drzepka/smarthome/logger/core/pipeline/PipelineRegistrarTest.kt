package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.PropertiesConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
internal class PipelineRegistrarTest {

    @Mock
    private lateinit var pipelineManager: PipelineManager

    @Test
    fun `should register pipeline from enabled source`() {
        val pipeline = mock<Pipeline>()
        val factory = TestPipelineFactory(SourceType.AFORE_T6, pipeline)
        val config = config(
            """
            source.dr.type=AFORE_T6
            source.dr.enabled=true
            source.dr.host=192.168.1.100
            """.trimIndent()
        )

        createRegistrar(config, listOf(factory)).registerAll()

        verify(pipelineManager).addPipeline(pipeline)
        then(factory.lastCreatedName).isEqualTo("dr")
    }

    @Test
    fun `should register pipeline when enabled property is absent`() {
        val pipeline = mock<Pipeline>()
        val factory = TestPipelineFactory(SourceType.AFORE_T6, pipeline)
        val config = config("source.dr.type=AFORE_T6")

        createRegistrar(config, listOf(factory)).registerAll()

        verify(pipelineManager).addPipeline(pipeline)
    }

    @Test
    fun `should skip disabled source`() {
        val factory = TestPipelineFactory(SourceType.AFORE_T6, mock())
        val config = config(
            """
            source.dr.type=AFORE_T6
            source.dr.enabled=false
            """.trimIndent()
        )

        createRegistrar(config, listOf(factory)).registerAll()

        verify(pipelineManager, never()).addPipeline(any())
        then(factory.lastCreatedName).isNull()
    }

    @Test
    fun `should register multiple pipelines from multiple sources`() {
        val pipeline1 = mock<Pipeline>()
        val pipeline2 = mock<Pipeline>()
        val factory1 = TestPipelineFactory(SourceType.AFORE_T6, pipeline1)
        val factory2 = TestPipelineFactory(SourceType.SMA, pipeline2)
        val config = config(
            """
            source.inverter1.type=AFORE_T6
            source.inverter2.type=SMA
            """.trimIndent()
        )

        createRegistrar(config, listOf(factory1, factory2)).registerAll()

        verify(pipelineManager).addPipeline(pipeline1)
        verify(pipelineManager).addPipeline(pipeline2)
    }

    @Test
    fun `should not register pipeline when factory returns null`() {
        val factory = TestPipelineFactory(SourceType.AFORE_T6, null)
        val config = config("source.dr.type=AFORE_T6")

        createRegistrar(config, listOf(factory)).registerAll()

        verify(pipelineManager, never()).addPipeline(any())
    }

    @Test
    fun `should do nothing when no sources are configured`() {
        createRegistrar(config("other.property=value"), emptyList()).registerAll()

        verify(pipelineManager, never()).addPipeline(any())
    }

    @Test
    fun `should throw when source type is unknown`() {
        val config = config("source.dr.type=UNKNOWN_TYPE")

        thenThrownBy { createRegistrar(config, emptyList()).registerAll() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("UNKNOWN_TYPE")
    }

    @Test
    fun `should throw when no factory is registered for source type`() {
        val config = config("source.dr.type=AFORE_T6")

        thenThrownBy { createRegistrar(config, emptyList()).registerAll() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("AFORE_T6")
    }

    @Test
    fun `should throw when duplicate factories are registered for the same type`() {
        val factory1 = TestPipelineFactory(SourceType.AFORE_T6, mock())
        val factory2 = TestPipelineFactory(SourceType.AFORE_T6, mock())

        thenThrownBy { createRegistrar(config(""), listOf(factory1, factory2)).registerAll() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("AFORE_T6")
    }

    private fun config(content: String): ConfigPropertySource = PropertiesConfigPropertySource(content)

    private fun createRegistrar(config: ConfigPropertySource, factories: List<PipelineFactory>) =
        PipelineRegistrar(config, factories, pipelineManager)

    private class TestPipelineFactory(
        override val sourceType: SourceType,
        private val pipeline: Pipeline?
    ) : PipelineFactory {
        var lastCreatedName: String? = null

        override fun create(name: String, properties: ConfigPropertySource): Pipeline? {
            lastCreatedName = name
            return pipeline
        }
    }
}
