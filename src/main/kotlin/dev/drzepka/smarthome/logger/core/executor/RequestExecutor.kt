package dev.drzepka.smarthome.logger.core.executor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.time.Duration

abstract class RequestExecutor(val baseUrl: String, private val timeout: Duration) {
    protected val client = HttpClient(Apache5) {
        install(ContentNegotiation) {
            jackson {
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }

        engine {
            connectTimeout = timeout.inWholeMilliseconds
            socketTimeout = timeout.inWholeMilliseconds.toInt()
        }
    }

    protected var authorization: String? = null

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    fun basicAuthorization(username: String, password: String) {
        val merged = "$username:$password"
        val encoded = Base64.getEncoder().encode(merged.toByteArray(StandardCharsets.UTF_8))
        authorization = "Basic ${encoded.toString(StandardCharsets.UTF_8)}"
    }

    protected suspend inline fun <reified Req : Any, reified Res> executeRequest(
        method: String,
        url: String,
        requestBody: Req?
    ): Res {
        try {
            return doExecuteRequest(method, url, requestBody)
        } catch (e: ConnectException) {
            throw ConnectionException(url, e)
        } catch (e: Exception) { // todo: don't assume it's a response exception, create dedicated catch block
            throw ResponseException(url, e)
        }
    }

    protected suspend inline fun <reified Req : Any, reified Res> doExecuteRequest(
        method: String,
        url: String,
        requestBody: Req?
    ): Res =
        client.request(baseUrl + url) {
            this.method = HttpMethod.parse(method)
            authorization?.let { header("Authorization", it) }

            requestBody?.let {
                contentType(ContentType.Application.Json)
                setBody(it)
            }
        }.body()
}
