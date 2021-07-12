package dev.drzepka.smarthome.logger.core.executor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*

abstract class RequestExecutor(val baseUrl: String, private val timeoutSeconds: Int = 3) {
    protected val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }

        engine {
            connectTimeout = timeoutSeconds * 1000
            socketTimeout = timeoutSeconds * 1000
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

    protected suspend inline fun <Req : Any, reified Res> executeRequest(
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

    protected suspend inline fun <Req : Any, reified Res> doExecuteRequest(
        method: String,
        url: String,
        requestBody: Req?
    ): Res {
        return client.request(baseUrl + url) {
            this.method = HttpMethod(method)
            authorization?.let { header("Authorization", it) }

            requestBody?.let {
                contentType(ContentType.Application.Json)
                this.body = it
            }
        }
    }
}