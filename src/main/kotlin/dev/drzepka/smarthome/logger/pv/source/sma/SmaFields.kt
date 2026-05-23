package dev.drzepka.smarthome.logger.pv.source.sma

/**
 * Source: https://github.com/kellerza/pysma
 */
object SmaFields {

    val fields: List<SmaField> by lazy {
        listOf(
            gridPower, frequency,
            powerL1, powerL2, powerL3,
            voltageL1, voltageL2, voltageL3,
            currentL1, currentL2, currentL3,
            pvPowerA, pvPowerB,
            pvVoltageA, pvVoltageB,
            pvCurrentA, pvCurrentB,
            dailyYield, totalYield
        )
    }

    // Grid
    val gridPower = SmaField("gridPower", "6100_40263F00", unit = "W")
    val frequency = SmaField("frequency", "6100_00465700", factor = 100, unit = "Hz")

    // Phase power
    val powerL1 = SmaField("powerL1", "6100_40464000", unit = "W")
    val powerL2 = SmaField("powerL2", "6100_40464100", unit = "W")
    val powerL3 = SmaField("powerL3", "6100_40464200", unit = "W")

    // Phase voltage
    val voltageL1 = SmaField("voltageL1", "6100_00464800", factor = 100, unit = "V")
    val voltageL2 = SmaField("voltageL2", "6100_00464900", factor = 100, unit = "V")
    val voltageL3 = SmaField("voltageL3", "6100_00464A00", factor = 100, unit = "V")

    // Phase current
    val currentL1 = SmaField("currentL1", "6100_40465300", factor = 1000, unit = "A")
    val currentL2 = SmaField("currentL2", "6100_40465400", factor = 1000, unit = "A")
    val currentL3 = SmaField("currentL3", "6100_40465500", factor = 1000, unit = "A")

    // PV strings (A = string 1, B = string 2)
    val pvPowerA = SmaField("pvPowerA", "6380_40251E00", keyIdx = 0, unit = "W")
    val pvPowerB = SmaField("pvPowerB", "6380_40251E00", keyIdx = 1, unit = "W")
    val pvVoltageA = SmaField("pvVoltageA", "6380_40451F00", keyIdx = 0, factor = 100, unit = "V")
    val pvVoltageB = SmaField("pvVoltageB", "6380_40451F00", keyIdx = 1, factor = 100, unit = "V")
    val pvCurrentA = SmaField("pvCurrentA", "6380_40452100", keyIdx = 0, factor = 1000, unit = "A")
    val pvCurrentB = SmaField("pvCurrentB", "6380_40452100", keyIdx = 1, factor = 1000, unit = "A")

    // Energy
    val dailyYield = SmaField("dailyYield", "6400_00262200", unit = "Wh")
    val totalYield = SmaField("totalYield", "6400_00260100", factor = 1000, unit = "kWh")
}
