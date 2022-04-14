package dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth

import dev.drzepka.smarthome.common.util.Logger
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.math.min

/**
 * Reads output produced by the bluetoothctl utility and divides it into tokens. Tokens are recognized based
 * on indentation.
 */
class InputReader(private val stream: InputStream) {

    private val log by Logger()
    private var currentTokenBuffer = ""
    private var tokenStartIndent = -1

    fun readTokens(): List<String> {
        val read = readAll()
        if (read.isEmpty())
            return emptyList()

        val tokens = ArrayList<String>()
        for (line in read.lines()) {
            if (shouldIgnoreLine(line))
                continue

            if (tokenStartIndent == -1) {
                currentTokenBuffer = line
                tokenStartIndent = getIndent(line)
                continue
            }

            val currentIndent = getIndent(line)
            if (currentIndent == 0 || currentIndent < tokenStartIndent) {
                // Previous token has been completely read
                tokens.add(currentTokenBuffer)
                currentTokenBuffer = ""
                tokenStartIndent = getIndent(line)
            }

            if (currentTokenBuffer.isNotEmpty())
                currentTokenBuffer += "\n"
            currentTokenBuffer += line
            log.trace("Read line: {}", line)
        }

        return tokens
    }

    fun readAll(): String {
        val sink = ByteArrayOutputStream()

        val buffer = ByteArray(1024)

        var available = min(stream.available(), buffer.size)
        while (available > 0) {
            stream.read(buffer, 0, available)
            sink.write(buffer, 0, available)

            Thread.sleep(10)
            available = min(stream.available(), buffer.size)
        }

        val raw = sink.toByteArray().toString(StandardCharsets.UTF_8)
        return ESCAPE_SEQUENCES_REGEX.replace(raw, "")
    }

    private fun shouldIgnoreLine(line: String): Boolean {
        return line.startsWith("[bluetooth]#") || line.isEmpty()
    }

    private fun getIndent(line: String): Int {
        var indent = 0
        for (char in line) {
            if (char != ' ' && char != '\t')
                break
            indent++
        }

        return indent
    }

    companion object {
        private val ESCAPE_SEQUENCES_REGEX = Regex("\\x1b\\[[0-9;]*[a-zA-Z]")
    }
}