package dev.drzepka.smarthome.logger.sensors.pipeline.collector

import com.diozero.api.I2CDeviceInterface
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import java.nio.ByteBuffer

/**
 * Datasheet: [link](https://dfimg.dfrobot.com/nobody/wiki/b37f8a2ff795acc7446c8defbb957054.PDF).
 *
 * Command line testing:
 * ```
 * # Wake up
 * i2ctransfer -y 1 w2@0x70 0x35 0x17
 * # Measure
 * i2ctransfer -y 1 w2@0x70 0x7c 0xa2 && i2ctransfer -y 1 r6@0x70
 * # Sleep
 * i2ctransfer -y 1 w2@0x70 0xb0 0x98
 * ```
 */
class SHTC3DataCollector(private val device: I2CDeviceInterface, private val mac: MacAddress) :
    DataCollector<Pair<MacAddress, ByteArray>> {
    private val log by Logger()

    override fun getData(): Collection<Pair<MacAddress, ByteArray>> {
        device.writeBytes(*COMMAND_WAKE_UP)
        Thread.sleep(1) // Wait for the device to wake up
        device.writeBytes(*COMMAND_MEASURE)

        val array = ByteArray(6)
        device.readBytes(array)
        val str = array.map { (it.toInt() and 0xff).toString(16) }
        log.info("Received data from SHTC3: $str") // todo: change to trace

        device.writeBytes(*COMMAND_SLEEP)

        // todo: if there's something wrong with reading measurements, try to soft-reset the device

        return listOf(Pair(mac, array))
    }

    companion object {
        private val COMMAND_WAKE_UP = createCommand(0x3517)
        private val COMMAND_SLEEP = createCommand(0xB098)
        private val COMMAND_MEASURE = createCommand(0x7CA2)

        private fun createCommand(raw: Int): ByteArray {
            val buffer = ByteBuffer.allocate(2)
            buffer.putShort(raw.toShort())
            return buffer.array()
        }
    }
}
