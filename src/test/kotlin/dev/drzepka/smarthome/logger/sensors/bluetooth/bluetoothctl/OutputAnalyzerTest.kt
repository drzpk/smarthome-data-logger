package dev.drzepka.smarthome.logger.sensors.bluetooth.bluetoothctl

import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.OutputAnalyzer
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class OutputAnalyzerTest {

    @Test
    fun `should extract service data`() {
        val tokens = listOf(
            "[CHG] Controller DC:A6:32:7F:3F:42 Discovering: yes",
            "[CHG] Device BC:C1:31:5C:FA:9C ServiceData Key: 0000fe95-0000-1000-8000-00805f9b34fb",
            "[CHG] Device BC:C1:31:5C:FA:9C ServiceData Value:\n" +
                    "  30 58 5b 05 dd 69 1d 5c 38 c1 a4 08              0X[..i.\\8..."
        )

        val analyzer = OutputAnalyzer()
        val result = analyzer.analyze(tokens)

        then(result).hasSize(1)
        then(result[0]).isInstanceOf(BluetoothServiceData::class.java)

        val bluetoothServiceData = result[0] as BluetoothServiceData
        then(bluetoothServiceData.mac).isEqualTo(MacAddress("BC:C1:31:5C:FA:9C"))

        val bytes = "30 58 5b 05 dd 69 1d 5c 38 c1 a4 08".split(" ").map { it.toInt(16).toByte() }.toByteArray()
        then(bluetoothServiceData.data).isEqualTo(bytes)
    }
}