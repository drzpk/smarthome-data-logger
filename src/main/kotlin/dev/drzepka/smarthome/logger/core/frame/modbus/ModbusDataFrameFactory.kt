package dev.drzepka.smarthome.logger.core.frame.modbus

class ModbusDataFrameFactory(private val registers: List<ModbusRegister<*>>) {

    fun createDataFrames(): Collection<ModbusDataFrame> {
        if (registers.isEmpty()) return emptyList()

        val sorted = registers.sortedBy { it.address }
        val frames = mutableListOf<ModbusDataFrame>()
        var groupStart = 0

        for (i in 1..sorted.size) {
            val createFrame = if (i != sorted.size) {
                val next = sorted[i]
                val groupStartAddr = sorted[groupStart].address
                val spanWithNext = next.address + next.registerCount - groupStartAddr

                spanWithNext > MAX_REGISTERS
            } else true

            if (createFrame) {
                val group = sorted.subList(groupStart, i)
                frames.add(ModbusDataFrame(group.first().address, group))
                groupStart = i
            }
        }

        return frames
    }

    companion object {
        const val MAX_REGISTERS = 125
    }
}
